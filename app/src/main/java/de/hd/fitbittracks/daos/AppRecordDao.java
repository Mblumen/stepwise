package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.entities.AppRecord;
import de.hd.fitbittracks.enums.AchievementType;

@Dao
public interface AppRecordDao {
    @Query("SELECT * FROM app_records")
    LiveData<List<AppRecord>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppRecord appRecord);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AppRecord> appRecords);

    @Update
    void update(AppRecord appRecord);
}
