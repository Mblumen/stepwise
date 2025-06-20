package de.hd.fitbittracks.ui.achievements;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import de.hd.fitbittracks.daos.AchievementDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.ui.BaseViewModel;

public class AchievementsViewModel extends BaseViewModel {
    private final AchievementDao achievementDao;
    private final LiveData<List<Achievement>> allAchievements;

    public AchievementsViewModel(@NonNull Application application) {
        super(application);
        achievementDao = AppDatabase.getInstance(application).achievementDao();
        allAchievements = achievementDao.getAll();

    }

    public LiveData<List<Achievement>> getAllAchievements() {
        return allAchievements;
    }

    public LiveData<Achievement> getAchiementById(long id) {
        return achievementDao.getById(id);
    }
}
