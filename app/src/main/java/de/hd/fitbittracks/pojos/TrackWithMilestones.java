package de.hd.fitbittracks.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;

public class TrackWithMilestones {
    @Embedded
    public Track track;

    @Relation(
            parentColumn = "id",
            entityColumn = "trackId"
    )
    public List<Milestone> milestones;
}