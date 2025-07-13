package de.hd.fitbittracks.ui.achievements;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.daos.AchievementDao;
import de.hd.fitbittracks.databinding.FragmentAchievementsBinding;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.enums.AchievementDifficulty;
import de.hd.fitbittracks.enums.AchievementType;
import de.hd.fitbittracks.enums.ListItemType;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.Separator;
import de.hd.fitbittracks.ui.BaseFragment;
import de.hd.fitbittracks.ui.MainSharedViewModel;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class AchievementsFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private AchievementAdapter adapter;
    private AchievementsViewModel viewModel;
    private AchievementDao achievementDao; // Initialize from your DB
    List<ListItem> previousList = new ArrayList<>();
    private KonfettiView konfettiView;
    private Party party;

    Set<AchievementType> expandedTypes = new HashSet<>(List.of(
            AchievementType.DISTANCE,
            AchievementType.STEPS,
            AchievementType.TRACKS_COMPLETED
    ));

    private AchievementFilter currentFilter = AchievementFilter.ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AchievementsViewModel.class);

        FragmentAchievementsBinding binding = FragmentAchievementsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.achievementsList;
        makeChipsEqualSize(binding);
        adapter = new AchievementAdapter(context, this::onSectionToggled);
        recyclerView.setAdapter(adapter);
        viewModel.getAllAchievements().observe(getViewLifecycleOwner(), newList -> {
            if(!previousList.isEmpty()) {
                for (ListItem listItem : newList) {
                    if(!listItem.getType().equals(ListItemType.ELEMENT)) continue;
                    Achievement achievement = (Achievement) listItem;
                    if (achievement.unlocked && !wasPreviouslyUnlocked(achievement, previousList)) {
                        // Trigger unlock UI
                        showAchievementDialog(achievement);
                    }
                }
            }
            previousList.clear();
            previousList.addAll(newList);
            updateUIWithFilteredList(newList, currentFilter);
        });
        MainSharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(MainSharedViewModel.class);
        sharedViewModel.openAchievementReachedEvent.observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                viewModel.getAchiementById(data).observe(getViewLifecycleOwner(), achievement -> {
                    if (achievement != null) {
                        showAchievementDialog(achievement);
                    }
                });
            }
        });
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        addClickListenerToChipGroup(binding.filterChipGroup, binding.chipAll);
        return root;
    }

    private void onSectionToggled(AchievementType type) {
        if (expandedTypes.contains(type)) {
            expandedTypes.remove(type);
        } else {
            expandedTypes.add(type);
        }
        updateUIWithFilteredList(previousList != null ? previousList : new ArrayList<>(), currentFilter);
    }

    private void makeChipsEqualSize(FragmentAchievementsBinding binding) {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int chipWidth = screenWidth / 3 - 23;

        Chip chipAll = binding.chipAll;
        chipAll.setLayoutParams(new ViewGroup.LayoutParams(chipWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        Chip chipUnlocked = binding.chipUnlocked;
        chipUnlocked.setLayoutParams(new ViewGroup.LayoutParams(chipWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        Chip chipLocked = binding.chipLocked;
        chipLocked.setLayoutParams(new ViewGroup.LayoutParams(chipWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addClickListenerToChipGroup(ChipGroup chipGroup, Chip selectedChip) {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
           Log.i("AchievementsFragment", "ChipGroup checked state changed: " + checkedIds);
            if (checkedIds.isEmpty()) return;

            int id = checkedIds.get(0);
            if (id == R.id.chip_unlocked) {
                applyFilter(AchievementFilter.UNLOCKED);
            } else if (id == R.id.chip_locked) {
                applyFilter(AchievementFilter.LOCKED);
            } else {
                applyFilter(AchievementFilter.ALL);
            }
        });

        chipGroup.clearCheck();
        selectedChip.setChecked(true);
    }

    private void applyFilter(AchievementFilter filter) {
        currentFilter = filter;
        updateUIWithFilteredList(previousList != null ? previousList : new ArrayList<>(), filter);
    }

    private void updateUIWithFilteredList(List<ListItem> allItems, AchievementFilter filter) {
        if(adapter == null) return;
        List<ListItem> filteredItems = new ArrayList<>();
        Separator<AchievementType> currentSeparator = null;
        Log.e("AchievementsFragment", "Updating UI with filter: " + filter);
        for (ListItem item : allItems) {
            if (item.getType() == ListItemType.SEPARATOR) {
                // Include separators only if following achievements will pass filter
                currentSeparator = (Separator<AchievementType>) item;
                // We'll decide to add separators later after filtering achievements below
            } else if (item.getType() == ListItemType.ELEMENT) {
                Achievement achievement = (Achievement) item;
                // Apply filter:
                if (filter == AchievementFilter.UNLOCKED && !achievement.unlocked) continue;
                if (filter == AchievementFilter.LOCKED && achievement.unlocked) continue;

                // Add separator if type changed and separator not added yet
                if(currentSeparator != null) {
                    Separator<AchievementType> newSeparator = new Separator<>(
                            currentSeparator.title,
                            currentSeparator.data,
                            currentSeparator.getGenericType()
                    );
                    newSeparator.isExpanded = expandedTypes.contains(achievement.type);
                    filteredItems.add(newSeparator);
                    currentSeparator = null; // Reset after adding
                }
                if(expandedTypes.contains(achievement.type)) filteredItems.add(achievement);
            }
        }
        // Update RecyclerView adapter
        adapter.submitList(filteredItems);
    }

    private boolean wasPreviouslyUnlocked(Achievement achievement, List<ListItem> oldList) {
        for (ListItem old : oldList) {
            if (!old.getType().equals(ListItemType.ELEMENT)) continue;
            Achievement oldAchievement = (Achievement) old;
            if (oldAchievement.id == achievement.id && oldAchievement.unlocked) {
                return true;
            }
        }
        return false;
    }

    public void showAchievementDialog(Achievement achievement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_achievement_unlocked, null);

        ImageView icon = view.findViewById(R.id.icon);
        if (achievement.difficulty == AchievementDifficulty.BRONZE) {
            icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.bronze));
        } else if (achievement.difficulty == AchievementDifficulty.SILVER) {
            icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.silver));
        } else if (achievement.difficulty == AchievementDifficulty.GOLD) {
            icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.gold));
        }
        TextView description = view.findViewById(R.id.description);
        konfettiView = view.findViewById(R.id.konfettiView);

        // Set content
        description.setText(achievement.title + "\n" + achievement.description);
        icon.setImageResource(R.drawable.trophy); // or load from achievement.icon

        // Start confetti
        EmitterConfig emitterConfig = new Emitter(3, TimeUnit.SECONDS).perSecond(80);

        party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeed(4f)
                .position(0.5, 0.0) // top center
                .timeToLive(3000L)
                .shapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .colors(List.of(Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN))
                .build();

        konfettiView.start(party);

        AlertDialog alertDialog = builder.setView(view)
                .setCancelable(false)
                .show();

        Window window = alertDialog.getWindow();
        if(window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> alertDialog.setCancelable(true), 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(konfettiView != null && party != null) konfettiView.stop(party);
    }
}
