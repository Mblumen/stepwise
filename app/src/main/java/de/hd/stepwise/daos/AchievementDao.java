package de.hd.stepwise.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;

import de.hd.stepwise.entities.Achievement;
import de.hd.stepwise.enums.AchievementType;

@Dao
public interface AchievementDao {

    @Query("SELECT * FROM achievement ORDER BY type ASC, difficulty ASC")
    LiveData<List<Achievement>> getAll();

/*    @Query("Update achievement SET localImagePath = :localImagePath WHERE id = :achievementId")
    void updateLocalImagePath(long achievementId, String localImagePath);*/
    @Query("SELECT * FROM achievement WHERE unlocked = 0")
    List<Achievement> getLockedAchievements();

    @Query("SELECT * FROM achievement WHERE `key` = :key LIMIT 1")
    Achievement getByKey(String key);

    @Upsert
    void insert(Achievement achievement);

    @Update
    void update(Achievement achievement);

    @Query("SELECT * FROM achievement WHERE type IN (:types)")
    List<Achievement> getAchievementsByType(List<AchievementType> types);

    @Query("SELECT * FROM achievement WHERE id = :id LIMIT 1")
    LiveData<Achievement> getById(long id);
}