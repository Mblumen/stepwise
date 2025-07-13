package de.hd.fitbittracks.ui.tracks;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;

import dagger.hilt.android.AndroidEntryPoint;
import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.BasicListItemSharedBinding;
import de.hd.fitbittracks.databinding.DetailsListItemSharedBinding;
import de.hd.fitbittracks.databinding.FragmentTracksBinding;
import de.hd.fitbittracks.databinding.ItemTrackBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.TrackWithMilestones;
import de.hd.fitbittracks.ui.BaseAdapter;
import de.hd.fitbittracks.ui.BaseFragment;
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
        TracksAdapter adapter = new TracksAdapter(context, viewModel, getViewLifecycleOwner(), this, this::openMilestone);
        recyclerView.setAdapter(adapter);
        viewModel.getAllTracks().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.observedResult.observe(getViewLifecycleOwner(), event -> {
            MethodResult result = event.getContentIfNotHandled();
            if(result != null) showCustomToast(result.message, result.status);
        });
        viewModel.getStepLength().observe(getViewLifecycleOwner(), stepLength -> {
            if(stepLength != null) {
                adapter.setStepLength(stepLength);
            }
        });
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        return root;
    }

    public void openMilestone(MilestoneWithTotalDistance milestone) {
        Bundle args = new Bundle();
        args.putLong("milestone_id", milestone.id);
        navController.navigate(R.id.nav_milestone, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}