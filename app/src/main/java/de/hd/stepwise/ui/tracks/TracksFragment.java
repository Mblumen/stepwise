package de.hd.stepwise.ui.tracks;

import static de.hd.stepwise.ui.ToastHelper.showCustomToast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.FragmentTracksBinding;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.ui.BaseFragment;

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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        TracksViewModel viewModel = new ViewModelProvider(this).get(TracksViewModel.class);

        binding = FragmentTracksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.tracksList;
        TracksAdapter adapter = new TracksAdapter(requireContext(), viewModel, getViewLifecycleOwner(), this, this::openMilestone);
        recyclerView.setAdapter(adapter);
        viewModel.getAllTracks().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.observedResult.observe(getViewLifecycleOwner(), event -> {
            MethodResult result = event.getContentIfNotHandled();
            if(result != null) showCustomToast(requireContext(), result.message, result.status, Toast.LENGTH_SHORT);
        });
        viewModel.getStepLength().observe(getViewLifecycleOwner(), stepLength -> {
            if(stepLength != null) {
                adapter.setStepLength(stepLength);
            }
        });
        viewModel.getShowLockedMilestones().observe(getViewLifecycleOwner(), showLocked -> {;
            adapter.showHiddenMilestones(showLocked);
        });
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        return root;
    }

    public void openMilestone(MilestoneWithTotalDistance milestone) {
        Bundle args = new Bundle();
        args.putLong("track_id", milestone.trackId);
        args.putLong("milestone_id", milestone.id);
        NavHostFragment.findNavController(this).navigate(R.id.nav_milestone, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}