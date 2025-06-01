package de.hd.fitbittracks.pojos;

import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.ui.milestones.MilestoneItem;

public class MilestoneWithStatus implements MilestoneItem {
    public Milestone milestone;
    public boolean isCompleted;

    @Override
    public Milestone getMilestone() {
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
