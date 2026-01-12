package de.hd.stepwise.ui.tracks;

import android.animation.LayoutTransition;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.BasicListItemSharedBinding;
import de.hd.stepwise.databinding.DetailsListItemSharedBinding;
import de.hd.stepwise.databinding.ItemTrackBinding;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.interfaces.MapsItemClickedListener;
import de.hd.stepwise.pojos.TrackWithMilestones;
import de.hd.stepwise.ui.BaseAdapter;
import de.hd.stepwise.ui.milestones.MilestoneListItemBaseAdapter;

public class TracksAdapter extends BaseAdapter<TrackWithMilestones, TracksAdapter.TrackViewHolder> {

    private final Context context;
    private final TracksViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    private RecyclerView recyclerView;

    private final MapsItemClickedListener mapsItemClickedListener;

    private final MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener;

    protected TracksAdapter(Context context, TracksViewModel viewModel, LifecycleOwner lifecycleOwner, MapsItemClickedListener mapsItemClickedListener, MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener) {

        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull TrackWithMilestones oldItem, @NonNull TrackWithMilestones newItem) {
                return oldItem.track.id == newItem.track.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull TrackWithMilestones oldItem, @NonNull TrackWithMilestones newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.mapsItemClickedListener = mapsItemClickedListener;
        this.onMilestoneClickListener = onMilestoneClickListener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTrackBinding binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        TrackViewHolder holder =  new TrackViewHolder(binding);
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(2000); // Set your custom duration in ms
        ((ViewGroup) holder.itemView).setLayoutTransition(transition);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {

        TrackWithMilestones trackWithMilestones = getItem(position);
        Track track = trackWithMilestones.track; // Extract the Track entity from the TrackWithMilestones
        System.out.println(trackWithMilestones.track.name);
        System.out.println(trackWithMilestones.milestones);
        int totalDistance = trackWithMilestones.milestones.get(trackWithMilestones.milestones.size() - 1).totalDistance;
        boolean isExpanded = position == viewModel.expandedPosition;
        holder.baseTitle.setText(track.name);
        if (track.localImagePath == null || !new File(track.localImagePath).exists()) {
            viewModel.downloadTrackImageIfNeeded(track);
        }

        Object model;
        if (track.localImagePath != null) {
            model = new File(track.localImagePath);
        } else {
            model = track.imageUrl;
        }
        Glide.with(holder.baseImageView)
                .load(model)
                .placeholder(R.drawable.avatar_1)
                .into(holder.baseImageView);

        String formattedDistance = formatDistance(totalDistance);
        holder.baseDistance.setText(context.getString(R.string.label_distance, formattedDistance));
        String formattedSteps = formatNumber((int) (totalDistance/stepLength));
        holder.baseSteps.setText(context.getString(R.string.label_steps,formattedSteps));
        holder.baseMilestonesCount.setText(context.getString(R.string.label_milestones, "" + trackWithMilestones.milestones.size()));

        holder.detailsTitle.setText(track.name);
        Glide.with(holder.detailsImageView)
                .load(model)
                .placeholder(R.drawable.avatar_1)
                .into(holder.detailsImageView);

        holder.detailsStart.setText(context.getString(R.string.label_start,track.startLocation));
        holder.detailsEnd.setText(context.getString(R.string.label_end,track.endLocation));
        holder.detailsSteps.setText(context.getString(R.string.label_steps,formattedSteps));
        holder.detailsDistance.setText(context.getString(R.string.label_distance,formattedDistance));
        holder.detailsMilestonesCount.setText(context.getString(R.string.label_milestones, "" + trackWithMilestones.milestones.size()));

        holder.baseLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        holder.expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if(isExpanded) {
            holder.selectButton.setOnClickListener(v -> {;
                viewModel.selectTrack(track.id);
            });
            TracksMilestoneListItemAdapter milestoneAdapter = new TracksMilestoneListItemAdapter(context, mapsItemClickedListener, viewModel, onMilestoneClickListener, stepLength, showLockedMilestones);
            if (holder.milestoneRecycler.getItemDecorationCount() == 0) {
                holder.milestoneRecycler.addItemDecoration(new MilestoneListItemBaseAdapter.DistanceTrackDecoration(context, holder.milestoneRecycler, milestoneAdapter));
            }
            holder.milestoneRecycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.milestoneRecycler.setAdapter(milestoneAdapter);
            viewModel.setTrackId(track.id);
            viewModel.getAllMilestones().observe(lifecycleOwner, milestoneAdapter::submitList);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = viewModel.expandedPosition;
            viewModel.expandedPosition = isExpanded ? -1 : position;
            int currentAdapterPosition = holder.getAbsoluteAdapterPosition();

            notifyItemChanged(oldPos);
            notifyItemChanged(position);

            if (!isExpanded && recyclerView != null) {
                recyclerView.post(() -> {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        layoutManager.scrollToPositionWithOffset(currentAdapterPosition, 0); // Align to top
                    }
                });
            }
        });
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }
    public static class TrackViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout baseLayout;
        private final ImageView baseImageView;
        private final TextView baseTitle;
        private final TextView baseSteps;
        private final TextView baseDistance;
        private final TextView baseMilestonesCount;
        private final CardView expandedLayout;
        private final ImageView detailsImageView;
        private final TextView detailsTitle;
        private final TextView detailsStart;
        private final TextView detailsEnd;
        private final TextView detailsSteps;
        private final TextView detailsDistance;
        private final TextView detailsMilestonesCount;
        private final RecyclerView milestoneRecycler;
        private final Button selectButton;

        public TrackViewHolder(ItemTrackBinding binding) {
            super(binding.getRoot());
            BasicListItemSharedBinding sharedBinding = binding.sharedBaseItem;
            baseLayout = sharedBinding.itemBase;
            baseImageView = sharedBinding.itemBaseImage;
            baseTitle = sharedBinding.itemBaseTitle;
            baseSteps = sharedBinding.itemBaseSteps;
            baseDistance = sharedBinding.itemBaseDistance;
            baseMilestonesCount = sharedBinding.itemBaseMilestones;
            expandedLayout = binding.trackItemDetails;

            DetailsListItemSharedBinding detailsSharedBinding = binding.sharedDetailsItem;
            detailsImageView = detailsSharedBinding.image;
            detailsTitle = detailsSharedBinding.title;
            detailsStart = detailsSharedBinding.start;
            detailsEnd = detailsSharedBinding.end;
            detailsSteps = detailsSharedBinding.steps;
            detailsDistance = detailsSharedBinding.distance;
            detailsMilestonesCount = detailsSharedBinding.milestones;

            milestoneRecycler = binding.trackItemDetailsMilestones;
            selectButton = binding.trackItemDetailsSelectButton;
        }
    }
}