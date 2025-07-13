package de.hd.fitbittracks.ui.tracks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.MilestoneWithStatusTimelineBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.ui.milestones.MilestoneListItemBaseAdapter;
import de.hd.fitbittracks.ui.tracksprogress.TracksProgressMilestoneListItemAdapter;

public class TracksMilestoneListItemAdapter extends MilestoneListItemBaseAdapter<MilestoneWithTotalDistance> {

    public TracksMilestoneListItemAdapter(Context context, MapsItemClickedListener mapsItemClickedListener, OnMilestoneClickListener onMilestoneClickListener, float stepLength) {
        super(context, new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull MilestoneWithTotalDistance oldItem, @NonNull MilestoneWithTotalDistance newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull MilestoneWithTotalDistance oldItem, @NonNull MilestoneWithTotalDistance newItem) {
                return oldItem.equals(newItem);
            }
        }, mapsItemClickedListener, onMilestoneClickListener, stepLength);
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

        if(item.unlocked) {
            holder.lockedContainer.setVisibility(View.GONE);
            holder.milestoneUnlocked.setVisibility(View.VISIBLE);
        } else {
            holder.lockedContainer.setVisibility(View.VISIBLE);
            holder.milestoneUnlocked.setVisibility(View.GONE);
            int stepsLeft = (int) Math.floor(item.totalDistance / stepLength + 0.5f);
            holder.milestoneLocked.setText(context.getString(R.string.unlock_by_walking_distance, formatDistance(item.totalDistance), stepsLeft));
        }
    }
}