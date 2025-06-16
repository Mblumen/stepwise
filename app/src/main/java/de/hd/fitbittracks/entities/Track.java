package de.hd.fitbittracks.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "tracks")
public class Track {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    @NonNull
    public String startLocation;

    @NonNull
    public String endLocation;

    public String image; // optional

    public int totalDistance; // approximate total distance in meters
    public int totalSteps; // approximate total steps to complete

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track that)) return false;

        if (id != that.id) return false;
        if (totalSteps != that.totalSteps) return false;
        if (!name.equals(that.name)) return false;
        if (!startLocation.equals(that.startLocation)) return false;
        if (!endLocation.equals(that.endLocation)) return false;
        return Objects.equals(image, that.image);
    }
}