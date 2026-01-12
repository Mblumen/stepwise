package de.hd.stepwise.ui.milestones;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;

public interface MilestoneItem {
    MilestoneWithTotalDistance getMilestone();

    boolean equals(Object o);
}
