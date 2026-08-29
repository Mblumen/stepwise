package de.hd.stepwise.ui.achievements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.FragmentRecordsBinding;
import de.hd.stepwise.pojos.TodayStepStatus;
import de.hd.stepwise.ui.BaseFragment;

public class AppRecordsFragment extends BaseFragment {

    private AchievementsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AchievementsViewModel.class);

        FragmentRecordsBinding binding = FragmentRecordsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recordsList;

        AppRecordsAdapter adapter = new AppRecordsAdapter();
        recyclerView.setAdapter(adapter);
        viewModel.getAllAppRecords().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.getTodayStepStatus().observe(getViewLifecycleOwner(),
                status -> bindTodayStatus(binding, status));
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.observeToday(LocalDate.now());
    }

    private void bindTodayStatus(FragmentRecordsBinding binding, TodayStepStatus status) {
        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
        String total = numberFormat.format(status.totalSteps);
        String target = numberFormat.format(status.targetSteps);
        binding.todayStepCount.setText(getString(R.string.today_step_count, total, target));
        binding.todayStepProgress.setMax(status.targetSteps);
        binding.todayStepProgress.setProgressCompat(status.progressSteps, true);

        String detail = status.goalReached
                ? getString(R.string.today_step_goal_reached)
                : getString(R.string.today_steps_remaining,
                        numberFormat.format(status.remainingSteps));
        binding.todayStepDetail.setText(detail);
        binding.todayStepCard.setContentDescription(
                getString(R.string.today_step_status_description, total, target, detail));
    }
}
