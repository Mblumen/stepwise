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
}