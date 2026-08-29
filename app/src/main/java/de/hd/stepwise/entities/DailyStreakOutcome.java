package de.hd.stepwise.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_streak_outcome")
public class DailyStreakOutcome {
    @PrimaryKey
    @NonNull
    public String date;
    public int goalSteps;
    public boolean qualified;
    public int reserveUsed;
    public int reserveAfter;

    public DailyStreakOutcome(@NonNull String date, int goalSteps, boolean qualified,
                              int reserveUsed, int reserveAfter) {
        this.date = date;
        this.goalSteps = goalSteps;
        this.qualified = qualified;
        this.reserveUsed = reserveUsed;
        this.reserveAfter = reserveAfter;
    }
}
