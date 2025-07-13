package de.hd.fitbittracks.ui.milestones;

import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;

public interface MilestoneItem {
    MilestoneWithTotalDistance getMilestone();

    boolean equals(Object o);
}
