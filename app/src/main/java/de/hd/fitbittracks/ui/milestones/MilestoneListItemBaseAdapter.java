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
import androidx.recyclerview.widget.RecyclerView;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.MilestoneWithStatusBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.MapsItem;
import de.hd.fitbittracks.ui.BaseAdapter;

public abstract class MilestoneListItemBaseAdapter<T extends MilestoneItem> extends BaseAdapter<T, MilestoneListItemBaseAdapter.MilestoneBaseViewHolder> {
    protected static final int TYPE_DEFAULT = 0;

    protected final MapsItemClickedListener mapsItemClickedListener;

    public interface OnMilestoneClickListener {
        void onItemClick(Milestone milestone);
    }
    private final OnMilestoneClickListener listener;
    public MilestoneListItemBaseAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, MapsItemClickedListener mapsItemClickedListener, OnMilestoneClickListener listener, float stepLength) {
        super(diffCallback);
        this.mapsItemClickedListener = mapsItemClickedListener;
        this.listener = listener;
        this.stepLength = stepLength;
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
        holder.description.setText(milestone.description);
        holder.milestoneImage.setImageResource(AppImage.getResIdFor(milestone.image));

        if(!milestone.mapsUrl.isEmpty() || (milestone.latitude > 0 && milestone.longitude > 0)) {
            holder.mapsButton.setOnClickListener(v -> {
                openMap(new MapsItem(milestone.mapsUrl, milestone.latitude, milestone.longitude, milestone.title));
            });
        } else {
            holder.mapsButton.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(milestone);
            }
        });
        onBindExtendedViewHolder(holder, milestoneItem);
    }

    protected abstract<E extends MilestoneBaseViewHolder> void onBindExtendedViewHolder(E holder, T item);

    public static class MilestoneBaseViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView steps;
        public TextView distance;
        public TextView description;
        public ImageView milestoneImage;

        public ImageButton mapsButton; // If you want to add an image button for actions

        public MilestoneBaseViewHolder(MilestoneWithStatusBinding binding) {
            super(binding.getRoot());
            milestoneImage = binding.milestoneImage;
            title = binding.milestoneTitle;
            steps = binding.milestoneSteps;
            distance = binding.milestoneDistance;
            description = binding.milestoneDescription;
            mapsButton = binding.milestoneMapButton;
        }
    }
}