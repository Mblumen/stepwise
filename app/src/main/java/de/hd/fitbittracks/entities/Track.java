package de.hd.fitbittracks.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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

    public int totalSteps; // approximate total steps to complete
}