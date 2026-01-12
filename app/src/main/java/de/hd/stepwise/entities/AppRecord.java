package de.hd.stepwise.entities;

import android.icu.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Locale;

import de.hd.stepwise.enums.RecordType;

@Entity(tableName = "app_records")
public class AppRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;
    @NonNull
    public String unit;
    @NonNull
    public RecordType type; // Type of the record, e.g., STEPS, DISTANCE, etc.

    public float value; // Optional, can be used for storing numeric values associated with the record
    public long trackId; // Optional, can be used to associate the record with a specific track
    public long timestamp; // Timestamp for when the record was created or last updated
    public String description; // Optional description for the record

    public AppRecord(@NonNull String name, @NonNull String unit, @NonNull RecordType type) {
        this.name = name;
        this.unit = unit;
        this.type = type;
    }

    @Ignore
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    @Ignore
    public String getValueWithUnit() {
        return String.format(Locale.getDefault(), "%.2f %s", value, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppRecord that)) return false;

        if (id != that.id) return false;
        if (Float.compare(that.value, value) != 0) return false;
        if (trackId != that.trackId) return false;
        if (timestamp != that.timestamp) return false;
        if (!name.equals(that.name)) return false;
        if (!unit.equals(that.unit)) return false;
        return type == that.type && description.equals(that.description);
    }

}