package de.hd.stepwise.daos;

import android.hardware.Sensor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import de.hd.stepwise.entities.DailySteps;
import de.hd.stepwise.enums.StepSource;

@Dao
public interface DailyStepsDao {

    @Upsert
    void insertOrUpdate(DailySteps entity);

    @Upsert
    void insertOrUpdateList(List<DailySteps> entities);

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    DailySteps getByDate(String date);

    @Query("SELECT * FROM daily_steps WHERE date = :date AND source = :source LIMIT 1")
    DailySteps getByDateAndSource(String date, StepSource source);

    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    LiveData<List<DailySteps>> observeAll();

    @Query("DELETE FROM daily_steps")
    void clear();

    @Query("SELECT * FROM daily_steps WHERE source = :source AND addedStepsSinceLastUpdate > 0 ORDER BY date DESC")
    LiveData<List<DailySteps>> observeWithUpdateBySource(StepSource source);

    @Query("Update daily_steps SET addedStepsSinceLastUpdate = 0 WHERE source = :source")
    void resetAddedStepsSinceLastUpdate(StepSource source);
}