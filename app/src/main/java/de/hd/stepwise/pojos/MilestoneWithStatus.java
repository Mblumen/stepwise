package de.hd.stepwise.pojos;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.ui.milestones.MilestoneItem;

public class MilestoneWithStatus implements MilestoneItem {
    public MilestoneWithTotalDistance milestone;
    public boolean isCompleted;

    public float distanceWalked;
    public int stepsWalked;

    @Override
    public MilestoneWithTotalDistance getMilestone() {
        return milestone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MilestoneWithStatus that)) return false;

        if (isCompleted != that.isCompleted) return false;
        return milestone.equals(that.milestone);
    }
}
