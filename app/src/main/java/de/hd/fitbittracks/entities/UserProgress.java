package de.hd.fitbittracks.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import de.hd.fitbittracks.enums.ProgressStatus;

@Entity(
        tableName = "user_progress",
        foreignKeys = @ForeignKey(
                entity = Track.class,
                parentColumns = "id",
                childColumns = "trackId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("trackId")}
)
public class UserProgress {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long trackId;

    public int stepsWalked;

    public ProgressStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProgress that)) return false;

        if (id != that.id) return false;
        if (trackId != that.trackId) return false;
        if (stepsWalked != that.stepsWalked) return false;
        return status == that.status;
    }
}