package de.hd.stepwise.ui.milestones;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;

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
import de.hd.stepwise.pojos.MilestoneExperience;
import de.hd.stepwise.pojos.MilestoneQuiz;
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
    private MilestoneBinding binding;
    private MilestoneAudioPlayer audioPlayer;
    private String loadedAudioPath;
    private final Handler playbackHandler = new Handler(Looper.getMainLooper());
    private final Runnable playbackProgress = new Runnable() {
        @Override
        public void run() {
            if (audioPlayer == null) return;
            binding.audioProgress.setProgress(audioPlayer.getCurrentPosition());
            if (audioPlayer.isPlaying()) playbackHandler.postDelayed(this, 250);
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MilestoneViewModel.class);
        binding = MilestoneBinding.inflate(inflater, container, false);
        holder = new MilestoneHolder(binding);
        long trackId = MilestoneFragmentArgs.fromBundle(getArguments()).getTrackId();
        long milestoneId = MilestoneFragmentArgs.fromBundle(getArguments()).getMilestoneId();
        long progressId = MilestoneFragmentArgs.fromBundle(getArguments()).getProgressId();
        hideRichContent();
        List<MilestoneImage> oldList = new ArrayList<>();
        viewModel.getMilestoneById(milestoneId).observe(getViewLifecycleOwner(), milestone -> {
            if (milestone != null) {
                holder.bind(milestone, this, viewModel);
                if(milestone.extraImages == null || milestone.extraImages.isEmpty()) {
                    binding.gallerySection.setVisibility(View.GONE);
                    return;
                }
                binding.gallerySection.setVisibility(View.VISIBLE);
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
        if (progressId > 0) {
            viewModel.getExperience(progressId, milestoneId).observe(
                    getViewLifecycleOwner(),
                    experience -> renderRichContent(experience, progressId, milestoneId));
        }
        viewModel.getStepLength().observe(getViewLifecycleOwner(), stepLength -> {
            if(stepLength != null) {
                holder.updateStepCount(stepLength);
            }
        });
        return binding.getRoot();
    }

    private void renderRichContent(MilestoneExperience experience, long progressId,
                                   long milestoneId) {
        hideRichContent();
        if (experience == null || !experience.canRevealRichContent()) return;
        MilestoneWithTotalDistance milestone = experience.milestone;

        if (MilestoneContentRules.hasText(milestone.localStampImagePath)) {
            binding.stampSection.setVisibility(View.VISIBLE);
            Glide.with(binding.stampImage)
                    .load(new File(milestone.localStampImagePath))
                    .into(binding.stampImage);
        }
        if (MilestoneContentRules.hasText(milestone.localAudioPath)
                || MilestoneContentRules.hasText(milestone.audioUrl)) {
            binding.audioSection.setVisibility(View.VISIBLE);
            binding.audioTranscript.setText(getString(
                    R.string.audio_text_alternative, milestone.description));
            if (MilestoneContentRules.hasText(milestone.localAudioPath)) {
                prepareAudio(milestone.localAudioPath);
            } else {
                showAudioFailure();
            }
        }
        if (milestone.discovery != null && MilestoneContentRules.hasText(milestone.discovery.title)
                && MilestoneContentRules.hasText(milestone.discovery.text)) {
            binding.discoverySection.setVisibility(View.VISIBLE);
            binding.discoveryTitle.setText(milestone.discovery.title);
            binding.discoveryText.setText(milestone.discovery.text);
            if (MilestoneContentRules.hasText(milestone.discovery.sourceUrl)) {
                binding.discoverySource.setVisibility(View.VISIBLE);
                binding.discoverySource.setOnClickListener(view -> startActivity(new Intent(
                        Intent.ACTION_VIEW, Uri.parse(milestone.discovery.sourceUrl))));
            }
        }
        if (MilestoneContentRules.validQuiz(milestone.quiz)) {
            renderQuiz(milestone.quiz, experience, progressId, milestoneId);
        }
    }

    private void renderQuiz(MilestoneQuiz quiz, MilestoneExperience experience,
                            long progressId, long milestoneId) {
        binding.quizSection.setVisibility(View.VISIBLE);
        binding.quizQuestion.setText(quiz.question);
        binding.quizAnswers.removeAllViews();
        boolean completed = experience.reachedMilestone.quizCompletedAt != null;
        for (int index = 0; index < quiz.answers.size(); index++) {
            RadioButton answer = new RadioButton(requireContext());
            answer.setId(View.generateViewId());
            answer.setTag(index);
            answer.setText(quiz.answers.get(index));
            answer.setEnabled(!completed);
            if (experience.reachedMilestone.selectedQuizAnswer != null
                    && experience.reachedMilestone.selectedQuizAnswer == index) {
                answer.setChecked(true);
            }
            binding.quizAnswers.addView(answer);
        }
        if (completed) {
            binding.quizSubmit.setEnabled(false);
            binding.quizFeedback.setText(getString(R.string.quiz_correct, quiz.explanation));
        } else {
            binding.quizSubmit.setEnabled(true);
            binding.quizSubmit.setOnClickListener(view -> {
                int checkedId = binding.quizAnswers.getCheckedRadioButtonId();
                RadioButton selected = binding.quizAnswers.findViewById(checkedId);
                if (selected == null) {
                    binding.quizFeedback.setText(R.string.quiz_select_answer);
                    return;
                }
                int selectedIndex = (int) selected.getTag();
                boolean correct = selectedIndex == quiz.correctAnswerIndex;
                binding.quizFeedback.setText(getString(correct
                        ? R.string.quiz_correct : R.string.quiz_incorrect, quiz.explanation));
                if (correct) binding.quizSubmit.setEnabled(false);
                viewModel.answerQuiz(progressId, milestoneId, selectedIndex, correct);
            });
        }
    }

    private void prepareAudio(String path) {
        if (path.equals(loadedAudioPath) && audioPlayer != null) return;
        releaseAudio();
        loadedAudioPath = path;
        binding.audioToggle.setEnabled(false);
        binding.audioStatus.setText(R.string.audio_loading);
        try {
            audioPlayer = new MilestoneAudioPlayer(path, new MilestoneAudioPlayer.Listener() {
                @Override public void onReady(int durationMillis) {
                    binding.audioProgress.setMax(durationMillis);
                    binding.audioToggle.setEnabled(true);
                    binding.audioToggle.setText(R.string.audio_play);
                    binding.audioStatus.setText("");
                }
                @Override public void onPlayingChanged(boolean playing) {
                    binding.audioToggle.setText(playing ? R.string.audio_pause : R.string.audio_play);
                    playbackHandler.removeCallbacks(playbackProgress);
                    if (playing) playbackHandler.post(playbackProgress);
                }
                @Override public void onFailure() { showAudioFailure(); }
            });
            binding.audioToggle.setOnClickListener(view -> audioPlayer.toggle());
            binding.audioProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && audioPlayer != null) audioPlayer.seekTo(progress);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override public void onStopTrackingTouch(SeekBar seekBar) { }
            });
        } catch (Exception exception) {
            showAudioFailure();
        }
    }

    private void showAudioFailure() {
        binding.audioToggle.setEnabled(false);
        binding.audioStatus.setText(R.string.audio_failed);
    }

    private void hideRichContent() {
        binding.stampSection.setVisibility(View.GONE);
        binding.audioSection.setVisibility(View.GONE);
        binding.discoverySection.setVisibility(View.GONE);
        binding.discoverySource.setVisibility(View.GONE);
        binding.quizSection.setVisibility(View.GONE);
    }

    private void releaseAudio() {
        playbackHandler.removeCallbacks(playbackProgress);
        if (audioPlayer != null) audioPlayer.release();
        audioPlayer = null;
        loadedAudioPath = null;
    }

    @Override
    public void onDestroyView() {
        releaseAudio();
        binding = null;
        holder = null;
        super.onDestroyView();
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
