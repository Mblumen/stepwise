package de.hd.stepwise.pojos;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.ReachedMilestone;

public class MilestoneExperience {
    public final MilestoneWithTotalDistance milestone;
    public final ReachedMilestone reachedMilestone;

    public MilestoneExperience(MilestoneWithTotalDistance milestone,
                               ReachedMilestone reachedMilestone) {
        this.milestone = milestone;
        this.reachedMilestone = reachedMilestone;
    }

    public boolean canRevealRichContent() {
        return milestone != null && reachedMilestone != null
                && reachedMilestone.milestoneId == milestone.id;
    }
}
