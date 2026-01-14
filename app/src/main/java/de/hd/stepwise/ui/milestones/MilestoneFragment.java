package de.hd.stepwise.ui.milestones;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.MilestoneBinding;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.interfaces.MapsItemClickedListener;
import de.hd.stepwise.pojos.MapsItem;
import de.hd.stepwise.pojos.MilestoneImage;
import de.hd.stepwise.ui.BaseFragment;
import de.hd.stepwise.ui.layouthelper.CarouselLayoutManager;
import de.hd.stepwise.ui.layouthelper.CenterSnapHelper;
import de.hd.stepwise.ui.layouthelper.OverlapDecoration;

public class MilestoneFragment extends BaseFragment {

    private MilestoneHolder holder;
    private CarouselLayoutManager layoutManager;
    private int focusedPosition = RecyclerView.NO_POSITION;
    private MilestoneViewModel viewModel;
    private SnapHelper snapHelper;
    private MilestoneImageAdapter imageAdapter;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MilestoneViewModel.class);
        MilestoneBinding binding = MilestoneBinding.inflate(inflater, container, false);
        holder = new MilestoneHolder(binding);
        long trackId = MilestoneFragmentArgs.fromBundle(getArguments()).getTrackId();
        long milestoneId = MilestoneFragmentArgs.fromBundle(getArguments()).getMilestoneId();
        List<MilestoneImage> oldList = new ArrayList<>();
        viewModel.getMilestoneById(milestoneId).observe(getViewLifecycleOwner(), milestone -> {
            if (milestone != null) {
                holder.bind(milestone, this, viewModel);
                if(milestone.extraImages.isEmpty()) return;
                recyclerView = holder.binding.imageGallery;

                if(imageAdapter == null) {
                    imageAdapter = new MilestoneImageAdapter(viewModel, trackId, milestoneId);
                    recyclerView.setAdapter(imageAdapter);
                    recyclerView.addItemDecoration(new OverlapDecoration(120));

                    //LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    layoutManager = new CarouselLayoutManager(requireContext());
                    recyclerView.setLayoutManager(layoutManager);
                    imageAdapter.setRecyclerView(recyclerView);
                    imageAdapter.setOnItemClickListener(this::scrollToCenter);
                    imageAdapter.setOnExpandButtonClickedListener(this::expandImage);
                    // Snap to center
                    if (snapHelper == null) {
                        snapHelper = new CenterSnapHelper(0);
                        snapHelper.attachToRecyclerView(recyclerView);
                    }

                    imageAdapter.submitList(new ArrayList<>(milestone.extraImages));
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
                            int[] snapDistance = snapHelper.calculateDistanceToFinalSnap(layoutManager, snappedView);
                            if (snapDistance != null) {
                                recyclerView.smoothScrollBy(snapDistance[0], snapDistance[1]);
                            }
                        }
                    });

                }
                //imageAdapter.submitList(new ArrayList<>(milestone.extraImages));
                List<MilestoneImage> newList = milestone.extraImages;

                // Compare oldList with newList
                for (int i = 0; i < newList.size(); i++) {
                    MilestoneImage newImage = newList.get(i);
                    MilestoneImage oldImage = i < oldList.size() ? oldList.get(i) : null;

                    if (!newImage.equals(oldImage)) {
                        // This image changed! Notify adapter
                        imageAdapter.updateImageAtPosition(i, newImage);
                        notifyImageChanged(newImage);
                    }
                }

                oldList.clear();
                oldList.addAll(newList);
            }
        });
        viewModel.getStepLength().observe(getViewLifecycleOwner(), stepLength -> {
            if(stepLength != null) {
                holder.updateStepCount(stepLength);
            }
        });
        return binding.getRoot();
    }

    private void notifyImageChanged(MilestoneImage updatedImage) {
        recyclerView.post(() -> {
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                int adapterPos = recyclerView.getChildAdapterPosition(child);
                if (adapterPos == RecyclerView.NO_POSITION) continue;

                if (imageAdapter.toRealPosition(adapterPos) == updatedImage.position) {
                    imageAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void scrollToCenter(int position) {
        int currentCenterPosition = getCurrentCenteredPosition();
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(requireContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                Log.i("MilestoneFragment", "Position: " + position + ", Current Center Position: " + currentCenterPosition);
                // Scrolling LEFT
                return Integer.compare(position, currentCenterPosition);   // Scrolling RIGHT
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
        private MilestoneWithTotalDistance milestone;
        private float stepLength = 1;
        protected final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        protected final DecimalFormat df = new DecimalFormat("#,##0.0");

        public MilestoneHolder(MilestoneBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MilestoneWithTotalDistance milestone, MapsItemClickedListener mapsItemClickedListener, MilestoneViewModel viewModel) {
            this.milestone = milestone;
            binding.title.setText(milestone.title);
            String formattedSteps = numberFormat.format((int)(milestone.totalDistance / stepLength));
            binding.stepCount.setText(formattedSteps);
            String formattedDistance = milestone.totalDistance >= 10000 ? df.format(milestone.totalDistance/1000.0) + " km" : numberFormat.format(milestone.totalDistance) + " m";
            binding.distance.setText(formattedDistance);
            binding.description.setText(milestone.description);
            if (milestone.localImagePath == null || !new File(milestone.localImagePath).exists()) {
                viewModel.downloadMilestoneImageIfNeeded(milestone);
            }

            Object model;
            if (milestone.localImagePath != null) {
                model = new File(milestone.localImagePath);
            } else {
                model = milestone.imageUrl;
            }
            Glide.with(binding.image)
                    .load(model)
                    .placeholder(R.drawable.avatar_1)
                    .into(binding.image);
            if((milestone.mapsUrl != null && !milestone.mapsUrl.isEmpty()) || (milestone.latitude > 0 && milestone.longitude > 0)) {
                binding.milestoneMapButton.setOnClickListener(v -> mapsItemClickedListener.onMapsItemClicked(new MapsItem(milestone.mapsUrl, milestone.latitude, milestone.longitude, milestone.title)));
            } else {
                binding.milestoneMapButton.setVisibility(View.GONE);
            }
        }
        public void updateStepCount(float stepLength) {
            this.stepLength = stepLength;
            if(milestone == null) return;
            int stepCount = (int) (milestone.totalDistance / stepLength);
            binding.stepCount.setText(numberFormat.format(stepCount));
        }
    }
}
