package de.hd.fitbittracks.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

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

    public int stepOffset; // Steps after which this milestone is reached

    public int stepCount;

    @NonNull
    public String title;

    @NonNull
    public String description;
    public String mapsUrl; // optional, for location-based milestones
    public double latitude; // optional, for location-based milestones
    public double longitude; // optional, for location-based milestones

    public String image; // optional

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Milestone that)) return false;

        if (id != that.id) return false;
        if (trackId != that.trackId) return false;
        if (stepOffset != that.stepOffset) return false;
        if (stepCount != that.stepCount) return false;
        if (!title.equals(that.title)) return false;
        if (!description.equals(that.description)) return false;
        return Objects.equals(image, that.image);
    }

    @Override
    public Milestone getMilestone() {
        return this;
    }
}