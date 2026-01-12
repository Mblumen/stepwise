package de.hd.stepwise.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
import java.util.Objects;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.Track;

public class TrackWithMilestones {
    @Embedded
    public Track track;

    @Relation(
            parentColumn = "id",
            entityColumn = "trackId"
    )
    public List<MilestoneWithTotalDistance> milestones;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackWithMilestones that)) return false;

        return Objects.equals(track, that.track);
    }
}