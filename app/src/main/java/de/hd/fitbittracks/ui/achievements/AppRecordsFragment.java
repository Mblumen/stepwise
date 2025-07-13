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
import de.hd.fitbittracks.databinding.FragmentRecordsBinding;
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

public class AppRecordsFragment extends BaseFragment {
    private AppRecordsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        AchievementsViewModel viewModel = new ViewModelProvider(this).get(AchievementsViewModel.class);

        FragmentRecordsBinding binding = FragmentRecordsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recordsList;

        adapter = new AppRecordsAdapter();
        recyclerView.setAdapter(adapter);
        viewModel.getAllAppRecords().observe(getViewLifecycleOwner(), adapter::submitList);
        // Assuming you have a way to get all milestones mapped by trackId
        adapter.setRecyclerView(recyclerView);
        return root;
    }
}
