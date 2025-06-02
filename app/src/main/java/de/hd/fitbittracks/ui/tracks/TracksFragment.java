package de.hd.fitbittracks.ui.tracks;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.BasicListItemSharedBinding;
import de.hd.fitbittracks.databinding.DetailsListItemSharedBinding;
import de.hd.fitbittracks.databinding.FragmentTracksBinding;
import de.hd.fitbittracks.databinding.ItemTrackBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.TrackWithMilestones;
import de.hd.fitbittracks.ui.BaseFragment;
import de.hd.fitbittracks.ui.milestones.MilestoneFragment;
import de.hd.fitbittracks.ui.milestones.MilestoneListItemBaseAdapter;

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
public class TracksFragment extends BaseFragment {

    public TracksFragment() {
        super();
    }

    private FragmentTracksBinding binding;
    private TracksViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TracksViewModel.class);

        binding = FragmentTracksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.tracksList;
        TracksAdapter adapter = new TracksAdapter(context, this, this::openMilestone);
        recyclerView.setAdapter(adapter);
        viewModel.getAllTracks().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.observedResult.observe(getViewLifecycleOwner(), event -> {
            MethodResult result = event.getContentIfNotHandled();
            if(result != null) showCustomToast(result.message, result.status);
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

    public static class MilestoneListItemAdapter extends MilestoneListItemBaseAdapter<Milestone> {

        public MilestoneListItemAdapter(Context context, MapsItemClickedListener mapsItemClickedListener, OnMilestoneClickListener onMilestoneClickListener) {
            super(context, new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Milestone oldItem, @NonNull Milestone newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Milestone oldItem, @NonNull Milestone newItem) {
                    return oldItem.equals(newItem);
                }
            }, mapsItemClickedListener, onMilestoneClickListener);
        }

        @Override
        protected void onBindExtendedViewHolder(MilestoneBaseViewHolder holder, Milestone item) {
            // No additional binding needed for this adapter
        }
    }

    public class TracksAdapter extends ListAdapter<TrackWithMilestones, TracksAdapter.TrackViewHolder> {

        private int expandedPosition = -1;
        private final Context context;

        private RecyclerView recyclerView;

        private final MapsItemClickedListener mapsItemClickedListener;

        private final MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener;

        protected TracksAdapter(Context context, MapsItemClickedListener mapsItemClickedListener, MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener) {
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
            boolean isExpanded = position == expandedPosition;
            holder.baseTitle.setText(track.name);
            holder.baseSteps.setText(context.getString(R.string.integer_count, track.totalSteps));
            holder.baseMilestonesCount.setText(context.getString(R.string.integer_count, trackWithMilestones.milestones.size()));
            holder.baseImageView.setImageResource(AppImage.getResIdFor(track.image));

            holder.detailsTitle.setText(track.name);
            holder.detailsImageView.setImageResource(AppImage.getResIdFor(track.image));
            holder.detailsStart.setText(track.startLocation);
            holder.detailsEnd.setText(track.endLocation);
            holder.detailsSteps.setText(context.getString(R.string.integer_count, track.totalSteps));

            holder.baseLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            holder.expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if(isExpanded) {
                holder.selectButton.setOnClickListener(v -> {;
                    viewModel.selectTrack(track.id);
                });
                MilestoneListItemAdapter milestoneAdapter = new MilestoneListItemAdapter(context, mapsItemClickedListener, onMilestoneClickListener);
                holder.milestoneRecycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                holder.milestoneRecycler.setAdapter(milestoneAdapter);
                viewModel.setTrackId(track.id);
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

        public void setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }
        public static class TrackViewHolder extends RecyclerView.ViewHolder {

            private final LinearLayout baseLayout;
            private final ImageView baseImageView;
            private final TextView baseTitle;
            private final TextView baseSteps;
            private final TextView baseMilestonesCount;
            private final CardView expandedLayout;
            private final ImageView detailsImageView;
            private final TextView detailsTitle;
            private final TextView detailsStart;
            private final TextView detailsEnd;
            private final TextView detailsSteps;
            private final RecyclerView milestoneRecycler;
            private final Button selectButton;

            public TrackViewHolder(ItemTrackBinding binding) {
                super(binding.getRoot());
                BasicListItemSharedBinding sharedBinding = binding.sharedBaseItem;
                baseLayout = sharedBinding.itemBase;
                baseImageView = sharedBinding.itemBaseImage;
                baseTitle = sharedBinding.itemBaseTitle;
                baseSteps = sharedBinding.itemBaseSteps;
                baseMilestonesCount = sharedBinding.itemBaseMilestones;
                expandedLayout = binding.trackItemDetails;

                DetailsListItemSharedBinding detailsSharedBinding = binding.sharedDetailsItem;
                detailsImageView = detailsSharedBinding.image;
                detailsTitle = detailsSharedBinding.title;
                detailsStart = detailsSharedBinding.start;
                detailsEnd = detailsSharedBinding.end;
                detailsSteps = detailsSharedBinding.steps;

                milestoneRecycler = binding.trackItemDetailsMilestones;
                selectButton = binding.trackItemDetailsSelectButton;
            }
        }
    }


}