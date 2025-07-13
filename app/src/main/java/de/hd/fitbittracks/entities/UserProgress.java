package de.hd.fitbittracks.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;

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
    public float distanceWalked; // in meters

    public ProgressStatus status;
    public Long startedAt; // timestamp in milliseconds
    public Long pausedAt;       // timestamp when progress was last paused
    public Long totalPausedTime; // total milliseconds spent paused
    public Long completedAt; // timestamp in milliseconds, null if not
    @Ignore
    public Long getActiveDuration() {
        if (startedAt == null || completedAt == null) return null;
        long pausedTime = totalPausedTime == null ? 0 : totalPausedTime;
        return completedAt - startedAt - pausedTime;
    }

    @Ignore
    public Long getPausedDuration() {
        if (pausedAt == null && totalPausedTime == null) return null;
        if( pausedAt == null) {
            // If pausedAt is null, it means the progress is currently not paused
            return totalPausedTime;
        }
        if (totalPausedTime == null) {
            // If totalPausedTime is null, it means no time has been paused yet
            return System.currentTimeMillis() - pausedAt;
        }
        return totalPausedTime + (System.currentTimeMillis() - pausedAt);
    }

    @Ignore
    public Long getTotalDuration() {
        if (startedAt == null || completedAt == null) return null;
        return completedAt - startedAt;
    }

    @Ignore
    public Long getCurrentActiveDuration() {
        if (startedAt == null) return null;
        long pausedTime = totalPausedTime == null ? 0 : totalPausedTime;
        return System.currentTimeMillis() - startedAt - pausedTime;
    }

    @Ignore
    public Long getCurrentTotalDuration() {
        if (startedAt == null) return null;
        return System.currentTimeMillis() - startedAt ;
    }

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