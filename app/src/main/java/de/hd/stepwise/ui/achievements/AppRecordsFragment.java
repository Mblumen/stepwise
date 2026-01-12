package de.hd.stepwise.ui.achievements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import de.hd.stepwise.databinding.FragmentRecordsBinding;
import de.hd.stepwise.ui.BaseFragment;

public class AppRecordsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        AchievementsViewModel viewModel = new ViewModelProvider(this).get(AchievementsViewModel.class);

        FragmentRecordsBinding binding = FragmentRecordsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recordsList;

        AppRecordsAdapter adapter = new AppRecordsAdapter();
        recyclerView.setAdapter(adapter);
        viewModel.getAllAppRecords().observe(getViewLifecycleOwner(), adapter::submitList);
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        return root;
    }
}
