package de.hd.fitbittracks.ui.milestones;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.MilestoneSharedBinding;
import de.hd.fitbittracks.databinding.MilestoneWithStatusBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.MapsItem;

public abstract class MilestoneBaseAdapter<T extends MilestoneItem> extends ListAdapter<T, MilestoneBaseAdapter.MilestoneBaseViewHolder> {
    protected static final int TYPE_DEFAULT = 0;
    private final Context context;

    protected final MapsItemClickedListener mapsItemClickedListener;

    public MilestoneBaseAdapter(Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback, MapsItemClickedListener mapsItemClickedListener) {
        super(diffCallback);
        this.context = context;
        this.mapsItemClickedListener = mapsItemClickedListener;
    }

    public void openMap(MapsItem mapsItem) {
        mapsItemClickedListener.onMapsItemClicked(mapsItem);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_DEFAULT; // override in subclass if needed
    }

    @NonNull
    @Override
    public MilestoneBaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MilestoneWithStatusBinding binding = MilestoneWithStatusBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MilestoneBaseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneBaseViewHolder holder, int position) {
        T milestoneItem = getItem(position);
        Milestone milestone = milestoneItem.getMilestone();
        holder.title.setText(milestone.title);
        holder.steps.setText(context.getString(R.string.integer_count, milestone.stepOffset));
        holder.description.setText(milestone.description);
        holder.milestoneImage.setImageResource(AppImage.getResIdFor(milestone.image));

        if(!milestone.mapsUrl.isEmpty() || (milestone.latitude > 0 && milestone.longitude > 0)) {
            holder.mapsButton.setOnClickListener(v -> {
                openMap(new MapsItem(milestone.mapsUrl, milestone.latitude, milestone.longitude, milestone.title));
            });
        } else {
            holder.mapsButton.setVisibility(View.GONE);
        }
        onBindExtendedViewHolder(holder, milestoneItem);
    }

    protected abstract<E extends MilestoneBaseViewHolder> void onBindExtendedViewHolder(E holder, T item);

    public static class MilestoneBaseViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView steps;
        TextView description;
        ImageView milestoneImage;

        ImageButton mapsButton; // If you want to add an image button for actions

        public MilestoneBaseViewHolder(MilestoneWithStatusBinding binding) {
            super(binding.getRoot());
            milestoneImage = binding.milestoneImage;
            title = binding.milestoneTitle;
            steps = binding.milestoneSteps;
            description = binding.milestoneDescription;
            mapsButton = binding.milestoneMapButton;
        }
    }
}