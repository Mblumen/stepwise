package de.hd.stepwise.pojos;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.ReachedMilestone;

public class MilestoneExperienceTest {
    @Test
    public void richContentRequiresReachForSameMilestone() {
        MilestoneWithTotalDistance milestone = new MilestoneWithTotalDistance();
        milestone.id = 12;

        assertFalse(new MilestoneExperience(milestone, null).canRevealRichContent());
        assertFalse(new MilestoneExperience(milestone,
                new ReachedMilestone(3, 13, 100, 10)).canRevealRichContent());
        assertTrue(new MilestoneExperience(milestone,
                new ReachedMilestone(3, 12, 100, 10)).canRevealRichContent());
    }
}
