package de.hd.fitbittracks.ui.milestones;

import static androidx.recyclerview.widget.LinearSmoothScroller.SNAP_TO_START;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import de.hd.fitbittracks.databinding.MilestoneBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.MapsItem;
import de.hd.fitbittracks.ui.BaseFragment;
import de.hd.fitbittracks.ui.layouthelper.CarouselLayoutManager;
import de.hd.fitbittracks.ui.layouthelper.CenterSnapHelper;
import de.hd.fitbittracks.ui.layouthelper.LowSensitivitySnapHelper;
import de.hd.fitbittracks.ui.layouthelper.OverlapDecoration;

public class MilestoneFragment extends BaseFragment {

    private MilestoneHolder holder;
    private CarouselLayoutManager layoutManager;
    private int focusedPosition = RecyclerView.NO_POSITION;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        MilestoneViewModel viewModel = new ViewModelProvider(this).get(MilestoneViewModel.class);
        MilestoneBinding binding = MilestoneBinding.inflate(inflater, container, false);
        holder = new MilestoneHolder(binding);
        MilestoneFragmentArgs args = MilestoneFragmentArgs.fromBundle(getArguments());
        long milestoneId = args.getMilestoneId();
        viewModel.getMilestoneById(milestoneId).observe(getViewLifecycleOwner(), milestone -> {
                if (milestone != null) {
                    holder.bind(milestone, this);
                    if(milestone.extraImages.isEmpty()) return;
                    RecyclerView recyclerView = holder.binding.imageGallery;
                    MilestoneImageAdapter imageAdapter = new MilestoneImageAdapter();
                    recyclerView.setAdapter(imageAdapter);
                    recyclerView.addItemDecoration(new OverlapDecoration(120));

                    //LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    layoutManager = new CarouselLayoutManager(requireContext());
                    recyclerView.setLayoutManager(layoutManager);
                    imageAdapter.setLayoutManager(layoutManager);
                    imageAdapter.setRecyclerView(recyclerView);
                    imageAdapter.setOnItemClickListener(this::scrollToCenter);
                    // Snap to center
                    SnapHelper snapHelper = new CenterSnapHelper(0);
                    snapHelper.attachToRecyclerView(recyclerView);

                    imageAdapter.submitList(milestone.extraImages);
                    int startPosition = Integer.MAX_VALUE / 2;
                    int offset = startPosition % milestone.extraImages.size();
                    recyclerView.scrollToPosition(startPosition - offset);
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                recyclerView.post(() -> {
                                    View snapView = snapHelper.findSnapView(layoutManager);
                                    if (snapView != null) {
                                        int position = recyclerView.getChildAdapterPosition(snapView);
                                        if (position != RecyclerView.NO_POSITION) {
                                            focusedPosition = position;
                                            imageAdapter.setFocusedPosition(position);
                                        }
                                    }
                                });
                            }
                        }
                    });
                    recyclerView.post(() -> {
                        View snappedView = snapHelper.findSnapView(layoutManager);
                        if (snappedView != null) {
                            int snapDistance[] = snapHelper.calculateDistanceToFinalSnap(layoutManager, snappedView);
                            if (snapDistance != null) {
                                recyclerView.smoothScrollBy(snapDistance[0], snapDistance[1]);
                            }
                        }
                    });
                }
            });
        viewModel.getSettings().observe(getViewLifecycleOwner(), settings -> {
            if(settings != null) {
                holder.updateStepCount(settings.stepLengthInMeters);
            }
        });
        View root = binding.getRoot();
        return root;
    }

    private void scrollToCenter(int position) {
        int currentCenterPosition = getCurrentCenteredPosition();
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(requireContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                Log.i("MilestoneFragment", "Position: " + position + ", Current Center Position: " + currentCenterPosition);
                if (position > currentCenterPosition) {
                    return SNAP_TO_END;   // Scrolling RIGHT
                } else if (position < currentCenterPosition) {
                    return SNAP_TO_START; // Scrolling LEFT
                } else {
                    return SNAP_TO_ANY;
                }
            }
        };
        smoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(smoothScroller);
    }

    private int getCurrentCenteredPosition() {
        return focusedPosition == RecyclerView.NO_POSITION ? 0 : focusedPosition;
    }

    private static class MilestoneHolder extends RecyclerView.ViewHolder {
        private final MilestoneBinding binding;
        private Milestone milestone;
        private float stepLength = 1;
        protected final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        protected final DecimalFormat df = new DecimalFormat("#,##0.0");

        public MilestoneHolder(MilestoneBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Milestone milestone, MapsItemClickedListener mapsItemClickedListener) {
            this.milestone = milestone;
            binding.title.setText(milestone.title);
            String formattedSteps = numberFormat.format((int)(milestone.distanceOffset / stepLength));
            binding.stepCount.setText(formattedSteps);
            String formattedDistance = milestone.distanceOffset >= 10000 ? df.format(milestone.distanceOffset/1000) + " km" : numberFormat.format(milestone.distanceOffset) + " m";
            binding.distance.setText(formattedDistance);
            binding.description.setText(milestone.description);
            binding.image.setImageResource(AppImage.getResIdFor(milestone.image));
            if(!milestone.mapsUrl.isEmpty() || (milestone.latitude > 0 && milestone.longitude > 0)) {
                binding.milestoneMapButton.setOnClickListener(v -> {
                    mapsItemClickedListener.onMapsItemClicked(new MapsItem(milestone.mapsUrl, milestone.latitude, milestone.longitude, milestone.title));
                });
            } else {
                binding.milestoneMapButton.setVisibility(View.GONE);
            }
        }
        public void updateStepCount(float stepLength) {
            this.stepLength = stepLength;
            if(milestone == null) return;
            int stepCount = (int) (milestone.distanceOffset / stepLength);
            binding.stepCount.setText(numberFormat.format(stepCount));
        }
    }
}
