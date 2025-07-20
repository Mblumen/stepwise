package de.hd.fitbittracks.ui.tracksprogress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import org.maplibre.android.MapLibre;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.FragmentTracksProgressBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.enums.ListItemType;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.Separator;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.ui.BaseFragment;
import de.hd.fitbittracks.ui.MainSharedViewModel;

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
public class TracksProgressFragment extends BaseFragment {

    private FragmentTracksProgressBinding binding;
    private TracksProgressViewModel viewModel;

    private TracksProgressAdapter adapter;

    private final Set<ProgressStatus> expandedTypes = new HashSet<>(List.of(
            ProgressStatus.ACTIVE,
            ProgressStatus.PAUSED
    ));

    List<ListItem> previousList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        //MapLibre.getInstance(context);
        viewModel = new ViewModelProvider(this).get(TracksProgressViewModel.class);

        binding = FragmentTracksProgressBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.progressList;

        adapter = new TracksProgressAdapter(context,  viewModel, getViewLifecycleOwner(), this, this::openMilestone, this::onSectionToggled);
        recyclerView.setAdapter(adapter);
        viewModel.getAllProgress().observe(getViewLifecycleOwner(), newList -> {
            previousList.clear();
            previousList.addAll(newList);
            updateUIWithFilteredList(newList);
        });
        viewModel.getStepLength().observe(getViewLifecycleOwner(), stepLength -> {
            if(stepLength != null) {
                adapter.setStepLength(stepLength);
            }
        });
        viewModel.observedResult.observe(getViewLifecycleOwner(), event -> {
            MethodResult result = event.getContentIfNotHandled();
            if(result != null) showCustomToast(result.message, result.status);
        });
        MainSharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(MainSharedViewModel.class);
        sharedViewModel.openTrackFinishedEvent.observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
               viewModel.finishTrack(data);
            }
        });
        sharedViewModel.openTrackWithProgressEvent.observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                adapter.setProgressId(data);
            }
        });
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        return root;
    }

    private void onSectionToggled(ProgressStatus status) {
        if(status == null) return;
        if(status.equals(ProgressStatus.ACTIVE)) {
            if (expandedTypes.contains(ProgressStatus.PAUSED)) {
                expandedTypes.remove(ProgressStatus.PAUSED);
            } else {
                expandedTypes.add(ProgressStatus.PAUSED);
            }
        }
        if (expandedTypes.contains(status)) {
            expandedTypes.remove(status);
        } else {
            expandedTypes.add(status);
        }

        updateUIWithFilteredList(previousList != null ? previousList : new ArrayList<>());
    }

    private void updateUIWithFilteredList(List<ListItem> allItems) {
        if(adapter == null) return;
        List<ListItem> filteredItems = new ArrayList<>();
        Separator<ProgressStatus> currentSeparator = null;
        for (ListItem item : allItems) {
            if (item.getType() == ListItemType.SEPARATOR) {
                currentSeparator = (Separator<ProgressStatus>) item;
            } else if (item.getType() == ListItemType.ELEMENT) {
                UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones = (UserProgressWithTrackAndMilestones) item;
                if(currentSeparator != null) {
                    Separator<ProgressStatus> newSeparator = new Separator<>(
                            currentSeparator.title,
                            currentSeparator.data,
                            currentSeparator.getGenericType()
                    );
                    newSeparator.isExpanded = expandedTypes.contains(userProgressWithTrackAndMilestones.userProgress.status);
                    filteredItems.add(newSeparator);
                    currentSeparator = null; // Reset after adding
                }
                if(expandedTypes.contains(userProgressWithTrackAndMilestones.userProgress.status)) filteredItems.add(userProgressWithTrackAndMilestones);
            }
        }
        // Update RecyclerView adapter
        adapter.submitList(filteredItems);
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