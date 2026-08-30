package de.hd.stepwise.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import de.hd.stepwise.entities.DailyActivity;

@Dao
public interface DailyActivityDao {
    @Upsert
    void insertOrUpdate(DailyActivity activity);

    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    DailyActivity getByDate(String date);

    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    LiveData<DailyActivity> observeByDate(String date);

    @Query("SELECT * FROM daily_activity ORDER BY date ASC")
    List<DailyActivity> getAll();

    @Query("SELECT * FROM daily_activity ORDER BY date ASC")
    LiveData<List<DailyActivity>> observeAll();

    @Query("SELECT COUNT(*) FROM daily_activity WHERE fitbitLastObserved IS NOT NULL")
    int countFitbitBaselines();
}
