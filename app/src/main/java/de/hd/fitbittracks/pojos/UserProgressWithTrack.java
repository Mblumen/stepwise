package de.hd.fitbittracks.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;

public class UserProgressWithTrack {
    @Embedded
    public UserProgress userProgress;

    @Relation(
            parentColumn = "trackId",
            entityColumn = "id"
    )
    public Track track;
}