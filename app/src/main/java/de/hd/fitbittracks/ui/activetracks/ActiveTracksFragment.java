package de.hd.fitbittracks.ui.activetracks;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.FragmentActiveTracksBinding;
import de.hd.fitbittracks.databinding.ItemTransformBinding;
import de.hd.fitbittracks.databinding.MilestoneBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.enums.AppImage;

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
public class ActiveTracksFragment extends Fragment {

    private FragmentActiveTracksBinding binding;
    private ActiveTracksViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ActiveTracksViewModel.class);

        binding = FragmentActiveTracksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerviewActivetracks;
        ActiveTracksAdapter adapter = new ActiveTracksAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        viewModel.getAllTracks().observe(getViewLifecycleOwner(), adapter::submitList);
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class MilestoneAdapter extends ListAdapter<Milestone, MilestoneAdapter.MilestoneViewHolder> {
        //private List<Milestone> milestones;
        private final Context context;

        public MilestoneAdapter(Context context) {
            super(new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Milestone oldItem, @NonNull Milestone newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Milestone oldItem, @NonNull Milestone newItem) {
                    return oldItem.title.equals(newItem.title);
                }
            });
            this.context = context;
        }

        @NonNull
        @Override
        public MilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MilestoneBinding binding = MilestoneBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MilestoneViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MilestoneViewHolder holder, int position) {
            Milestone milestone = getItem(position);
            holder.title.setText(milestone.title);
            holder.steps.setText(context.getString(R.string.integer_count, milestone.stepOffset));
            holder.description.setText(milestone.description);
            holder.milestoneImage.setImageResource(AppImage.getResIdFor(milestone.image));
        }



        public static class MilestoneViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView steps;
            TextView description;
            ImageView milestoneImage;

            public MilestoneViewHolder(MilestoneBinding binding) {
                super(binding.getRoot());
                title = binding.milestoneTitle;
                steps = binding.milestoneSteps;
                description = binding.milestoneDescription;
                milestoneImage = binding.milestoneImage;
            }
        }
    }

    public class ActiveTracksAdapter extends ListAdapter<Track, ActiveTracksAdapter.ActiveTrackViewHolder> {
        private int expandedPosition = -1;
        private final Context context;

        private RecyclerView recyclerView;

        protected ActiveTracksAdapter(Context context) {
            super(new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Track oldItem, @NonNull Track newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Track oldItem, @NonNull Track newItem) {
                    return oldItem.name.equals(newItem.name);
                }
            });
            this.context = context;
        }

        @NonNull
        @Override
        public ActiveTrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemTransformBinding binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            ActiveTrackViewHolder holder =  new ActiveTrackViewHolder(binding);
            LayoutTransition transition = new LayoutTransition();
            transition.setDuration(2000); // Set your custom duration in ms
            ((ViewGroup) holder.itemView).setLayoutTransition(transition);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ActiveTrackViewHolder holder, int position) {

            Track track = getItem(position);
            boolean isExpanded = position == expandedPosition;
            holder.baseTitle.setText(track.name);
            holder.baseImageView.setImageResource(AppImage.getResIdFor(track.image));

            holder.detailsTitle.setText(track.name);
            holder.detailsImageView.setImageResource(AppImage.getResIdFor(track.image));
            holder.detailsStart.setText(track.startLocation);
            holder.detailsEnd.setText(track.endLocation);
            holder.detailsSteps.setText(context.getString(R.string.integer_count, track.totalSteps));

            holder.baseLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            holder.expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if(isExpanded) {
                MilestoneAdapter milestoneAdapter = new MilestoneAdapter(context);
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
        public static class ActiveTrackViewHolder extends RecyclerView.ViewHolder {

            private final LinearLayout baseLayout;
            private final CardView expandedLayout;

            private final ImageView baseImageView;
            private final TextView baseTitle;
            private final ImageView detailsImageView;
            private final TextView detailsTitle;
            private final TextView detailsStart;
            private final TextView detailsEnd;
            private final TextView detailsSteps;
            private final RecyclerView milestoneRecycler;

            public ActiveTrackViewHolder(ItemTransformBinding binding) {
                super(binding.getRoot());
                baseLayout = binding.activeTrackItemBase;
                expandedLayout = binding.activeTrackItemDetails;

                baseImageView = binding.activeTrackItemBaseImage;
                baseTitle = binding.activeTrackItemBaseTitle;

                detailsImageView = binding.activeTrackItemDetailsImage;
                detailsTitle = binding.activeTrackItemDetailsTitle;
                detailsStart = binding.activeTrackItemDetailsStart;
                detailsEnd = binding.activeTrackItemDetailsEnd;
                detailsSteps = binding.activeTrackItemDetailsSteps;
                milestoneRecycler = binding.activeTrackItemDetailsMilestones;
            }
        }
    }


}