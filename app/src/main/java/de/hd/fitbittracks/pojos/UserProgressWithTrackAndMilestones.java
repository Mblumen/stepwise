package de.hd.fitbittracks.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;

public class UserProgressWithTrackAndMilestones {
    @Embedded
    public UserProgress userProgress;

    @Relation(
            entity = Track.class,
            parentColumn = "trackId",
            entityColumn = "id"
    )
    public TrackWithMilestones trackWithMilestones;
}