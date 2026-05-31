package de.hd.stepwise.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import de.hd.stepwise.enums.StepSource;

@Entity(tableName = "step_event")
public class StepEvent {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public int steps = 0;

    @NonNull
    @ColumnInfo(name = "source", defaultValue = "0")
    public StepSource source;

    public long timestamp = 0;
    public boolean handled;

    public StepEvent() {

    }

    @Ignore
    public StepEvent(int steps, StepSource source, long timestamp) {
        this.steps = steps;
        this.source = source;
        this.timestamp = timestamp;
    }
}
