package de.hd.stepwise.entities;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReachedMilestoneTest {

    @Test
    public void newReachRecordsCurrentTime() {
        long before = System.currentTimeMillis();

        ReachedMilestone reachedMilestone = new ReachedMilestone(1, 2, 300);

        long after = System.currentTimeMillis();
        assertTrue(reachedMilestone.reachedAt >= before);
        assertTrue(reachedMilestone.reachedAt <= after);
    }
}
