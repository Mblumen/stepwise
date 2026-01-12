package de.hd.stepwise.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

import de.hd.stepwise.pojos.TrackRoute;

@Entity(tableName = "track")
public class Track {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    @NonNull
    public String startLocation = "";
    @NonNull
    public String endLocation = "";

    public String imageUrl; // optional
    public String localImagePath; // optional, local path to image stored on device

    public TrackRoute trackRoute; // optional, URL to the route on a map service
    public long challengeDuration; // in seconds, optional

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track that)) return false;

        return id == that.id
                && name.equals(that.name)
                && startLocation.equals(that.startLocation)
                && endLocation.equals(that.endLocation)
                && Objects.equals(localImagePath, that.localImagePath)
                && Objects.equals(imageUrl, that.imageUrl);
    }
}