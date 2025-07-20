package de.hd.fitbittracks.ui.tracksprogress;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import org.maplibre.android.maps.MapView;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.MilestoneWithStatusTimelineBinding;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;
import de.hd.fitbittracks.ui.milestones.MilestoneListItemBaseAdapter;

public class TracksProgressMilestoneListItemAdapter extends MilestoneListItemBaseAdapter<MilestoneWithStatus>{
    public TracksProgressMilestoneListItemAdapter(Context context, MapsItemClickedListener mapsItemClickedListener, MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener, float stepLength) {
        super(context, new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull MilestoneWithStatus oldItem, @NonNull MilestoneWithStatus newItem) {
                return oldItem.milestone.id == newItem.milestone.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull MilestoneWithStatus oldItem, @NonNull MilestoneWithStatus newItem) {
                return oldItem.equals(newItem);
            }
        }, mapsItemClickedListener, onMilestoneClickListener, stepLength);
    }

    @NonNull
    @Override
    public MilestoneProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MilestoneWithStatusTimelineBinding binding = MilestoneWithStatusTimelineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MilestoneProgressViewHolder(binding);
    }

    @Override
    protected void onBindExtendedViewHolder(MilestoneListItemBaseAdapter.MilestoneBaseViewHolder holder, MilestoneWithStatus item) {
        MilestoneProgressViewHolder progressViewHolder = ((MilestoneProgressViewHolder) holder);
        String formattedDistance = formatDistanceProgress(item.distanceWalked, item.milestone.totalDistance);
        holder.distance.setText(formattedDistance);
        if(item.milestone.totalDistance - item.distanceWalked > 0) {
            String formattedSteps = formatSteps(item.stepsWalked, (int) Math.floor((item.milestone.totalDistance - item.distanceWalked) / stepLength + 0.5f));
            holder.steps.setText(formattedSteps);
        }
        else {
            if(item.stepsWalked >= 0) holder.steps.setText(context.getString(R.string.integer_count, item.stepsWalked));
            else holder.steps.setVisibility(View.GONE);
        }
        if(item.isCompleted) {
            progressViewHolder.lockedContainer.setVisibility(View.GONE);
            progressViewHolder.milestoneUnlocked.setVisibility(View.VISIBLE);
            progressViewHolder.milestoneStatusBadge.setVisibility(View.VISIBLE);
            progressViewHolder.milestoneProgress.setVisibility(View.GONE);
            addOpenMilestoneClickListener(holder, item.milestone);
        } else {
            progressViewHolder.lockedContainer.setVisibility(View.VISIBLE);
            progressViewHolder.milestoneUnlocked.setVisibility(View.GONE);
            progressViewHolder.milestoneStatusBadge.setVisibility(View.GONE);
            float distanceLeft = item.milestone.totalDistance - item.distanceWalked;
            int stepsLeft = (int) Math.floor(distanceLeft / stepLength + 0.5f);
            progressViewHolder.milestoneLocked.setText(context.getString(R.string.unlock_by_walking_distance, formatDistance(distanceLeft), stepsLeft));
            progressViewHolder.milestoneProgress.setVisibility(View.VISIBLE);
            progressViewHolder.milestoneProgress.setMax(item.milestone.distanceOffsetToPrevious);
            progressViewHolder.milestoneProgress.setProgress((int) (item.milestone.distanceOffsetToPrevious - (item.milestone.totalDistance - item.distanceWalked)));

        }
    }

    public static class MilestoneProgressViewHolder extends MilestoneListItemBaseAdapter.MilestoneBaseViewHolder {
        ImageView milestoneStatusBadge;
        private final ProgressBar milestoneProgress;
        public MilestoneProgressViewHolder(MilestoneWithStatusTimelineBinding binding) {
            super(binding);
            milestoneStatusBadge = milestone.milestoneStatusBadge;
            milestoneProgress = milestone.milestoneProgress;
        }
    }
}
