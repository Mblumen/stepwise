package de.hd.stepwise.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.AchievementDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.Achievement;
import de.hd.stepwise.enums.AchievementType;
import de.hd.stepwise.pojos.ListItem;
import de.hd.stepwise.pojos.Separator;

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

 /*   public void updateAchievementImagePath(long achievementId, String localImagePath) {
        achievementDao.updateLocalImagePath(achievementId, localImagePath);
    }*/
}
