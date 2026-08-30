package de.hd.stepwise.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_step_goal")
public class DailyStepGoal {
    @PrimaryKey
    @NonNull
    public String effectiveDate;
    public int steps;

    public DailyStepGoal(@NonNull String effectiveDate, int steps) {
        this.effectiveDate = effectiveDate;
        this.steps = steps;
    }
}
