package de.hd.stepwise.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_activity")
public class DailyActivity {
    @PrimaryKey
    @NonNull
    public String date;

    public int sensorSteps;
    public int fitbitSteps;

    @Nullable
    public Integer fitbitLastObserved;

    public DailyActivity(@NonNull String date) {
        this.date = date;
    }

    public int getTotalSteps() {
        return sensorSteps + fitbitSteps;
    }
}
