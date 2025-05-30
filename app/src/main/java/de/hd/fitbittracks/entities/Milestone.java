package de.hd.fitbittracks.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "milestones",
        foreignKeys = @ForeignKey(
                entity = Track.class,
                parentColumns = "id",
                childColumns = "trackId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("trackId")}
)
public class Milestone {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long trackId;

    public int stepOffset; // Steps after which this milestone is reached

    public int stepCount;

    @NonNull
    public String title;

    @NonNull
    public String description;

    public String image; // optional
}