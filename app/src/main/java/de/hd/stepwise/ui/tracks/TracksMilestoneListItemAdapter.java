package de.hd.stepwise.ui.tracks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.MilestoneWithStatusTimelineBinding;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.interfaces.MapsItemClickedListener;
import de.hd.stepwise.ui.milestones.MilestoneListItemBaseAdapter;
import de.hd.stepwise.ui.tracksprogress.TracksProgressMilestoneListItemAdapter;

public class TracksMilestoneListItemAdapter extends MilestoneListItemBaseAdapter<MilestoneWithTotalDistance> {
    private final boolean showLockedMilestones;
    public TracksMilestoneListItemAdapter(Context context, MapsItemClickedListener mapsItemClickedListener, BaseTracksViewModel trackViewModel, OnMilestoneClickListener onMilestoneClickListener, float stepLength, boolean showLockedMilestones) {
        super(context, new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull MilestoneWithTotalDistance oldItem, @NonNull MilestoneWithTotalDistance newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull MilestoneWithTotalDistance oldItem, @NonNull MilestoneWithTotalDistance newItem) {
                return oldItem.equals(newItem);
            }
        }, mapsItemClickedListener, trackViewModel, onMilestoneClickListener, stepLength);
        this.showLockedMilestones = showLockedMilestones;
    }

    @NonNull
    @Override
    public TracksProgressMilestoneListItemAdapter.MilestoneProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MilestoneWithStatusTimelineBinding binding = MilestoneWithStatusTimelineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TracksProgressMilestoneListItemAdapter.MilestoneProgressViewHolder(binding);
    }

    @Override
    protected void onBindExtendedViewHolder(MilestoneBaseViewHolder holder, MilestoneWithTotalDistance item) {
        String formattedSteps = formatNumber((int) (item.totalDistance/stepLength));
        holder.steps.setText(formattedSteps);
        String formattedDistance = formatDistance(item.totalDistance);
        holder.distance.setText(formattedDistance);

        if(item.unlocked || showLockedMilestones) {
            holder.lockedContainer.setVisibility(View.GONE);
            holder.milestoneUnlocked.setVisibility(View.VISIBLE);
            addOpenMilestoneClickListener(holder, item);
        } else {
            holder.lockedContainer.setVisibility(View.VISIBLE);
            holder.milestoneUnlocked.setVisibility(View.GONE);
            int stepsLeft = (int) Math.floor(item.totalDistance / stepLength + 0.5f);
            holder.milestoneLocked.setText(context.getString(R.string.unlock_by_walking_distance, formatDistance(item.totalDistance), stepsLeft));
        }
    }
}