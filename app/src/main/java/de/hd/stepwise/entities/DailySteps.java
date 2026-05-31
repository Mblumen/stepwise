package de.hd.stepwise.entities;

import android.hardware.Sensor;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import de.hd.stepwise.enums.StepSource;

@Entity(tableName = "daily_steps")
public class DailySteps {

    @PrimaryKey
    @NonNull
    public String date;

    public int steps = 0;
    @NonNull
    @ColumnInfo(name = "source", defaultValue = "0")
    public StepSource source;

    public long lastUpdated = 0;
    public int addedStepsSinceLastUpdate;

    public DailySteps() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DailySteps that)) return false;

        return this.date.equals(that.date)
                && this.steps == that.steps
                && this.source == that.source
                && this.lastUpdated == that.lastUpdated
                && this.addedStepsSinceLastUpdate == that.addedStepsSinceLastUpdate;
    }
}