package de.hd.stepwise.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
import java.util.Objects;

import de.hd.stepwise.entities.Track;
import de.hd.stepwise.entities.UserProgress;
import de.hd.stepwise.entities.UserProgressMilestoneStatus;
import de.hd.stepwise.enums.ListItemType;

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
        if(!Objects.equals(trackWithMilestones.track.localImagePath, that.trackWithMilestones.track.localImagePath)) return false;
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