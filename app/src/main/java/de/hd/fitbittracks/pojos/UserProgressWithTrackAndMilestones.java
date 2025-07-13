package de.hd.fitbittracks.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
import java.util.Objects;

import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.entities.UserProgressMilestoneStatus;
import de.hd.fitbittracks.enums.ListItemType;

public class UserProgressWithTrackAndMilestones implements ListItem{
    @Embedded
    public UserProgress userProgress;

    @Relation(
            entity = Track.class,
            parentColumn = "trackId",
            entityColumn = "id"
    )
    public TrackWithMilestones trackWithMilestones;

    @Relation(
            entity = UserProgressMilestoneStatus.class,
            parentColumn = "id",
            entityColumn = "progressId"
    )
    public List<UserProgressMilestoneStatus> userProgressMilestoneStatus;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProgressWithTrackAndMilestones that)) return false;

        return Objects.equals(userProgress, that.userProgress);
    }

    @Override
    public long getId() {
        return userProgress.id;
    }

    @Override
    public ListItemType getType() {
        return ListItemType.ELEMENT;
    }
}