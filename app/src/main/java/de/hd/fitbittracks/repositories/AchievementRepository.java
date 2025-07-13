package de.hd.fitbittracks.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.fitbittracks.daos.AchievementDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.enums.AchievementType;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.Separator;
import de.hd.fitbittracks.ui.achievements.AchievementFilter;

@Singleton
public class AchievementRepository {
    private final AchievementDao achievementDao;

    @Inject
    public AchievementRepository(AppDatabase db) {
        this.achievementDao = db.achievementDao();
    }

    public LiveData<List<ListItem>> getAchievementsWithSeparators() {
        return Transformations.map(achievementDao.getAll(), achievements -> {
            List<ListItem> result = new ArrayList<>();
            AchievementType currentType = null;
            for (Achievement achievement : achievements) {
                if(achievement.type != currentType) {
                    result.add(new Separator<>(achievement.type.displayName, achievement.type, AchievementType.class));
                }
                result.add(achievement);
                currentType = achievement.type;
            }
            return result;
        });
    }
}
