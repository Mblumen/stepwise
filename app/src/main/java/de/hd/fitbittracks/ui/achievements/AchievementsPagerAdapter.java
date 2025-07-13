package de.hd.fitbittracks.ui.achievements;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AchievementsPagerAdapter extends FragmentStateAdapter {
    public AchievementsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new AchievementsFragment() : new AppRecordsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}