package de.hd.stepwise.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Objects;

import de.hd.stepwise.pojos.MilestoneImage;

@Entity(
        tableName = "milestone",
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

    public int distanceOffsetToPrevious; //in meters, distance from previous milestone
    @NonNull
    public String title = "";
    @NonNull
    public String description = "";
    public String mapsUrl; // optional, for location-based milestones
    public double latitude; // optional, for location-based milestones
    public double longitude; // optional, for location-based milestones

    public String imageUrl; // optional
    public String localImagePath; // optional, local path to image stored on device
    public boolean unlocked = false; // default is locked

    @ColumnInfo(name = "extra_images")
    public List<MilestoneImage> extraImages;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Milestone that)) return false;

        if (id != that.id) return false;
        if (trackId != that.trackId) return false;
        if (!title.equals(that.title)) return false;
        if (!description.equals(that.description)) return false;
        if (!Objects.equals(localImagePath, that.localImagePath)) return false;
        return Objects.equals(imageUrl, that.imageUrl);
    }
}