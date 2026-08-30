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
        viewModel.setToday(LocalDate.now());
    }

    private void bindTodayStatus(FragmentRecordsBinding binding, TodayStepStatus status) {
        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
        String total = numberFormat.format(status.totalSteps);
        String target = numberFormat.format(status.targetSteps);
        binding.todayStepCount.setText(getString(R.string.today_step_count, total, target));
        binding.todayStepProgress.setMax(status.targetSteps);
        binding.todayStepProgress.setProgressCompat(status.progressSteps, true);

        String detail;
        if (status.goalReached && status.projectedReserveAdded > 0) {
            detail = getString(R.string.today_goal_reserve_added,
                    numberFormat.format(status.projectedReserveAdded));
        } else if (status.goalReached) {
            detail = getString(R.string.today_step_goal_reached);
        } else if (status.reserveSufficient) {
            detail = getString(R.string.today_steps_reserve_available,
                    numberFormat.format(status.remainingSteps));
        } else {
            detail = getString(R.string.today_steps_reserve_insufficient,
                    numberFormat.format(status.remainingSteps));
        }
        binding.todayStepDetail.setText(detail);
        binding.todayStepReserve.setText(getString(R.string.step_reserve,
                numberFormat.format(status.projectedReserveSteps), target));
        binding.todayStepCard.setContentDescription(
                getString(R.string.today_step_status_description, total, target, detail));
    }
}
