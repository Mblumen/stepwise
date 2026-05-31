package de.hd.stepwise.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import de.hd.stepwise.enums.StepSource;

@Entity(tableName = "user_settings")
public class UserSettings {
    @PrimaryKey(autoGenerate = false)
    public int id = 1; // Singleton entry

    public float stepLengthInMeters = 0.75f; // Default value, e.g. 75 cm
    // Add more fields as needed (e.g. units, notifications enabled)

    public boolean showCompletedTracks = true; // Default to showing completed tracks
    public boolean useDarkMode = true; // Default to light mode
    public boolean showLockedMilestones = false;

    public int refreshTimeInMinutesFitbit = 5;

    @NonNull
    @ColumnInfo(name = "stepSource", defaultValue = "0")
    public StepSource stepSource = StepSource.STEP_COUNTER;

}