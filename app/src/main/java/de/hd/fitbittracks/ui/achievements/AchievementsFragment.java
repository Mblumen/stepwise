package de.hd.fitbittracks.ui.achievements;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.daos.AchievementDao;
import de.hd.fitbittracks.databinding.FragmentAchievementsBinding;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.enums.AchievementDifficulty;
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
    List<Achievement> previousList = new ArrayList<>();
    private KonfettiView konfettiView;
    private Party party;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AchievementsViewModel.class);

        FragmentAchievementsBinding binding = FragmentAchievementsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.achievementsList;
        AchievementAdapter adapter = new AchievementAdapter(context);
        recyclerView.setAdapter(adapter);
        viewModel.getAllAchievements().observe(getViewLifecycleOwner(), newList -> {
            if(!previousList.isEmpty()) {
                for (Achievement achievement : newList) {
                    if (achievement.unlocked && !wasPreviouslyUnlocked(achievement, previousList)) {
                        // Trigger unlock UI
                        showAchievementDialog(achievement);
                    }
                }
            }
            previousList.clear();
            previousList.addAll(newList);
            adapter.submitList(newList);
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
        return root;
    }

    private boolean wasPreviouslyUnlocked(Achievement achievement, List<Achievement> oldList) {
        for (Achievement old : oldList) {
            if (old.id == achievement.id && old.unlocked) {
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
