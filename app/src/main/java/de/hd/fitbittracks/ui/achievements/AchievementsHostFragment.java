package de.hd.fitbittracks.ui.achievements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import de.hd.fitbittracks.databinding.FragmentAchievementsContainerBinding;
import de.hd.fitbittracks.ui.BaseFragment;

public class AchievementsHostFragment extends BaseFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        FragmentAchievementsContainerBinding binding = FragmentAchievementsContainerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ViewPager2 viewPager = binding.viewPager;
        TabLayout tabLayout = binding.tabLayout;

        AchievementsPagerAdapter adapter = new AchievementsPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Achievements" : "Records")
        ).attach();

        return view;
    }
}