package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import java.util.List;

import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.enums.AchievementType;

@Dao
public interface AchievementDao {

    @Query("SELECT * FROM achievements")
    LiveData<List<Achievement>> getAll();

    @Query("SELECT * FROM achievements WHERE unlocked = 0")
    List<Achievement> getLockedAchievements();

    @Query("SELECT * FROM achievements WHERE `key` = :key LIMIT 1")
    Achievement getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Achievement achievement);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Achievement> achievements);

    @Update
    void update(Achievement achievement);

    @Query("SELECT * FROM achievements WHERE type = :type")
    List<Achievement> getAchievementsByType(AchievementType type);

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    LiveData<Achievement> getById(long id);
}