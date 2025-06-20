package de.hd.fitbittracks.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Objects;

import de.hd.fitbittracks.pojos.MilestoneImage;
import de.hd.fitbittracks.ui.milestones.MilestoneItem;

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
public class Milestone implements MilestoneItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long trackId;

    public int distanceOffset; // Optional, for location-based milestones
    @NonNull
    public String title = "";
    @NonNull
    public String description = "";
    public String mapsUrl; // optional, for location-based milestones
    public double latitude; // optional, for location-based milestones
    public double longitude; // optional, for location-based milestones

    public String image; // optional
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
        return Objects.equals(image, that.image);
    }

    @Override
    public Milestone getMilestone() {
        return this;
    }
}