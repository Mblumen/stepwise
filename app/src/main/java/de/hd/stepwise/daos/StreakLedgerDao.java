package de.hd.stepwise.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import de.hd.stepwise.entities.DailyStepGoal;
import de.hd.stepwise.entities.DailyStreakOutcome;

@Dao
public interface StreakLedgerDao {
    @Query("SELECT * FROM daily_step_goal ORDER BY effectiveDate ASC")
    List<DailyStepGoal> getGoals();

    @Query("SELECT * FROM daily_step_goal ORDER BY effectiveDate ASC")
    LiveData<List<DailyStepGoal>> observeGoals();

    @Upsert
    void upsertGoal(DailyStepGoal goal);

    @Query("DELETE FROM daily_step_goal WHERE effectiveDate > :date")
    void deleteGoalsAfter(String date);

    @Query("SELECT * FROM daily_streak_outcome ORDER BY date ASC")
    LiveData<List<DailyStreakOutcome>> observeOutcomes();

    @Query("DELETE FROM daily_streak_outcome")
    void deleteOutcomes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOutcomes(List<DailyStreakOutcome> outcomes);
}
