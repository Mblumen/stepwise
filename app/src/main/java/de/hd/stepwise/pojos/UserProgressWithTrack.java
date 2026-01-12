package de.hd.stepwise.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.Objects;

import de.hd.stepwise.entities.Track;
import de.hd.stepwise.entities.UserProgress;

public class UserProgressWithTrack {
    @Embedded
    public UserProgress userProgress;

    @Relation(
            parentColumn = "trackId",
            entityColumn = "id"
    )
    public Track track;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProgressWithTrack that)) return false;

        return Objects.equals(userProgress, that.userProgress);
    }
}