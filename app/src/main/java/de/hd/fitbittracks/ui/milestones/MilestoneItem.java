package de.hd.fitbittracks.ui.milestones;

import de.hd.fitbittracks.entities.Milestone;

public interface MilestoneItem {
    Milestone getMilestone();

    boolean equals(Object o);
}
