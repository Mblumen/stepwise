package de.hd.fitbittracks.ui.tracksprogress;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.DetailsListItemSharedBinding;
import de.hd.fitbittracks.databinding.FragmentTracksProgressBinding;
import de.hd.fitbittracks.databinding.ItemProgressBinding;
import de.hd.fitbittracks.databinding.ListSeparatorBinding;
import de.hd.fitbittracks.databinding.MilestoneWithStatusBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.enums.ListItemType;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;
import de.hd.fitbittracks.pojos.Separator;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.ui.BaseAdapter;
import de.hd.fitbittracks.ui.BaseFragment;
import de.hd.fitbittracks.ui.milestones.MilestoneListItemBaseAdapter;

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
public class TracksProgressFragment extends BaseFragment {

    private FragmentTracksProgressBinding binding;
    private TracksProgressViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TracksProgressViewModel.class);

        binding = FragmentTracksProgressBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.progressList;
        //int firstActivePosition = viewModel.getActiveProgressPosition();
        long progressId = TracksProgressFragmentArgs.fromBundle(getArguments()).getProgressId();
        long milestoneId = TracksProgressFragmentArgs.fromBundle(getArguments()).getMilestoneId();
        Log.e("TracksProgressFragment", "Progress ID: " + progressId + ", Milestone ID: " + milestoneId);
        ProgressAdapter adapter = new ProgressAdapter(context, this, progressId, milestoneId, this::openMilestone);
        recyclerView.setAdapter(adapter);
        viewModel.getAllProgress().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.getSettings().observe(getViewLifecycleOwner(), settings -> {
            if(settings != null) {
                adapter.setStepLength(settings.stepLengthInMeters);
            }
        });
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        return root;
    }

    public void openMilestone(Milestone milestone) {
        Bundle args = new Bundle();
        args.putLong("milestone_id", milestone.id);
        navController.navigate(R.id.nav_milestone, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class MilestoneListItemAdapter extends MilestoneListItemBaseAdapter<MilestoneWithStatus> {

        public MilestoneListItemAdapter(MapsItemClickedListener mapsItemClickedListener, OnMilestoneClickListener onMilestoneClickListener, float stepLength) {
            super(new DiffUtil.ItemCallback<>() {
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
            MilestoneWithStatusBinding binding = MilestoneWithStatusBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MilestoneProgressViewHolder(binding);
        }

        @Override
        protected void onBindExtendedViewHolder(MilestoneBaseViewHolder holder, MilestoneWithStatus item) {
            MilestoneProgressViewHolder progressViewHolder = ((MilestoneProgressViewHolder) holder);
            String formattedDistance = formatDistanceProgress(item.distanceWalked, item.milestone.distanceOffset);
            holder.distance.setText(formattedDistance);
            if(item.milestone.distanceOffset - item.distanceWalked > 0) {
                String formattedSteps = formatSteps(item.stepsWalked, Math.round((item.milestone.distanceOffset - item.distanceWalked) / stepLength + 0.5f));
                holder.steps.setText(formattedSteps);
            }
            else {
                holder.steps.setVisibility(View.GONE);
            }
            if(item.isCompleted) {
                progressViewHolder.milestoneStatusBadge.setVisibility(View.VISIBLE);
            } else {
                progressViewHolder.milestoneStatusBadge.setVisibility(View.GONE);
            }
        }

        public static class MilestoneProgressViewHolder extends MilestoneBaseViewHolder {
            ImageView milestoneStatusBadge;

            public MilestoneProgressViewHolder(MilestoneWithStatusBinding binding) {
                super(binding);
                milestoneStatusBadge = binding.milestoneStatusBadge;
            }
        }
    }

    public class ProgressAdapter extends BaseAdapter<ListItem, RecyclerView.ViewHolder> {
        private int expandedPosition = RecyclerView.NO_POSITION;
        private final Context context;

        private RecyclerView recyclerView;

        private final MapsItemClickedListener mapsItemClickedListener;

        private long progressId;
        private long milestoneId;

        private final MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener;

        protected ProgressAdapter(Context context, MapsItemClickedListener mapsItemClickedListener, long progressId, long milestoneId, MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener) {
            super(new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                    return oldItem.getId() == newItem.getId() && oldItem.getType() == newItem.getType();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                    if (oldItem.getType() != newItem.getType()) return false;
                    return oldItem.equals(newItem);
                }
            });
            this.context = context;
            this.mapsItemClickedListener = mapsItemClickedListener;
            this.progressId = progressId;
            this.milestoneId = milestoneId;
            this.onMilestoneClickListener = onMilestoneClickListener;
        }

        @Override
        public int getItemViewType(int position) {
            ListItem item = getItem(position);
            return item.getType().key;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           if(viewType == ListItemType.SEPARATOR.key) {
                ListSeparatorBinding binding = ListSeparatorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new ListSeparatorViewHolder(binding);
            }
            else {
                ItemProgressBinding binding = ItemProgressBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                ProgressViewHolder holder = new ProgressViewHolder(binding);
                LayoutTransition transition = new LayoutTransition();
                transition.setDuration(2000); // Set your custom duration in ms
                ((ViewGroup) holder.itemView).setLayoutTransition(transition);
                return holder;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder genericHolder, int position) {
            if(getItemViewType(position) == ListItemType.SEPARATOR.key) {
                ListSeparatorViewHolder separatorHolder = (ListSeparatorViewHolder) genericHolder;
                Separator separator = (Separator) getItem(position);
                if(separator != null) {
                    separatorHolder.bind(separator);
                }
                return;
            }

            if(progressId > 0 && getItem(position).getId() == progressId) {
                // Automatically expand the item if it matches the progressId
                progressId = -1;
                expandedPosition = genericHolder.getAbsoluteAdapterPosition();
            }

            ProgressViewHolder holder = (ProgressViewHolder) genericHolder;
            UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones = (UserProgressWithTrackAndMilestones) getItem(position);
            boolean isExpanded = position == expandedPosition;
            holder.baseTitle.setText(userProgressWithTrackAndMilestones.trackWithMilestones.track.name);
            holder.baseImageView.setImageResource(AppImage.getResIdFor(userProgressWithTrackAndMilestones.trackWithMilestones.track.image));
            String formattedDistance = formatDistanceProgress(userProgressWithTrackAndMilestones.userProgress.distanceWalked, userProgressWithTrackAndMilestones.trackWithMilestones.track.totalDistance);
            holder.baseDistance.setText(formattedDistance);
            String formattedSteps = formatSteps(userProgressWithTrackAndMilestones.userProgress.stepsWalked, Math.round((userProgressWithTrackAndMilestones.trackWithMilestones.track.totalDistance - userProgressWithTrackAndMilestones.userProgress.distanceWalked)/ stepLength + 0.5f));
            holder.baseSteps.setText(formattedSteps);
            holder.baseMilestoneCount.setText(context.getString(R.string.integer_count, userProgressWithTrackAndMilestones.trackWithMilestones.milestones.size()));

            holder.detailsTitle.setText(userProgressWithTrackAndMilestones.trackWithMilestones.track.name);
            holder.detailsImageView.setImageResource(AppImage.getResIdFor(userProgressWithTrackAndMilestones.trackWithMilestones.track.image));
            holder.detailsStart.setText(userProgressWithTrackAndMilestones.trackWithMilestones.track.startLocation);
            holder.detailsEnd.setText(userProgressWithTrackAndMilestones.trackWithMilestones.track.endLocation);
            holder.detailsDistance.setText(formattedDistance);
            holder.detailsSteps.setText(formattedSteps);

            holder.baseLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            holder.expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.baseLayout.setSelected(userProgressWithTrackAndMilestones.userProgress.status == ProgressStatus.ACTIVE);
            holder.itemSelectedAccent.setVisibility(userProgressWithTrackAndMilestones.userProgress.status == ProgressStatus.ACTIVE ? View.VISIBLE : View.GONE);

            if(isExpanded) {
                MilestoneListItemAdapter milestoneAdapter = new MilestoneListItemAdapter(mapsItemClickedListener, onMilestoneClickListener, stepLength);
                holder.milestoneRecycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                holder.milestoneRecycler.setAdapter(milestoneAdapter);
                viewModel.setTrack(userProgressWithTrackAndMilestones.trackWithMilestones.track);
                viewModel.setDistanceWalked(userProgressWithTrackAndMilestones.userProgress.distanceWalked);
                viewModel.setStepsWalked(userProgressWithTrackAndMilestones.userProgress.stepsWalked);
                viewModel.getAllMilestones().observe(getViewLifecycleOwner(), milestoneAdapter::submitList);
            }

            holder.itemView.setOnClickListener(v -> {
                int oldPos = expandedPosition;
                expandedPosition = isExpanded ? -1 : position;
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

        private void expand(int position) {
            Log.e("expand", "Expanding position: " + position);
            expandedPosition = position;
        }

        public void setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        public static class ListSeparatorViewHolder extends RecyclerView.ViewHolder {
            private final TextView separatorText;

            public ListSeparatorViewHolder(ListSeparatorBinding binding) {
                super(binding.getRoot());
                separatorText = binding.separatorText;
            }

            public void bind(Separator separator) {
                separatorText.setText(separator.title);
            }
        }
        public static class ProgressViewHolder extends RecyclerView.ViewHolder {

            private final LinearLayout baseLayout;
            private final ImageView baseImageView;
            private final TextView baseTitle;
            private final TextView baseSteps;
            private final TextView baseDistance;

            private final TextView baseMilestoneCount;

            private final View itemSelectedAccent;

            private final CardView expandedLayout;
            private final ImageView detailsImageView;
            private final TextView detailsTitle;
            private final TextView detailsStart;
            private final TextView detailsEnd;
            private final TextView detailsSteps;
            private final TextView detailsDistance;
            private final RecyclerView milestoneRecycler;

            public ProgressViewHolder(ItemProgressBinding binding) {
                super(binding.getRoot());
                baseLayout = binding.progressItemBase;
                baseImageView = binding.progressItemBaseImage;
                baseTitle = binding.progressItemBaseTitle;
                baseSteps = binding.progressItemBaseSteps;
                baseDistance = binding.progressItemBaseDistance;
                baseMilestoneCount = binding.progressItemBaseMilestones;
                itemSelectedAccent = binding.itemSelectedAccent;

                expandedLayout = binding.progressItemDetails;

                DetailsListItemSharedBinding detailsSharedBinding = binding.sharedDetailsItem;

                detailsImageView = detailsSharedBinding.image;
                detailsTitle = detailsSharedBinding.title;
                detailsStart = detailsSharedBinding.start;
                detailsEnd = detailsSharedBinding.end;
                detailsSteps = detailsSharedBinding.steps;
                detailsDistance = detailsSharedBinding.distance;

                milestoneRecycler = binding.progressDetailsMilestones;
            }
        }
    }


}