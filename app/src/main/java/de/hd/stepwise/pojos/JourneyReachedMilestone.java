package de.hd.stepwise.pojos;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.ReachedMilestone;

public class JourneyReachedMilestone {
    public final MilestoneWithTotalDistance milestone;
    public final ReachedMilestone reached;

    public JourneyReachedMilestone(MilestoneWithTotalDistance milestone,
                                   ReachedMilestone reached) {
        this.milestone = milestone;
        this.reached = reached;
    }
}
