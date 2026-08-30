package de.hd.stepwise.pojos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TodayStepStatusTest {

    @Test
    public void zeroStepsShowsTheWholeTargetRemaining() {
        TodayStepStatus status = new TodayStepStatus(0, 5_000);

        assertEquals(0, status.totalSteps);
        assertEquals(0, status.progressSteps);
        assertEquals(5_000, status.remainingSteps);
        assertFalse(status.goalReached);
    }

    @Test
    public void stepsBelowTargetShowProgressAndRemainingSteps() {
        TodayStepStatus status = new TodayStepStatus(3_420, 5_000);

        assertEquals(3_420, status.progressSteps);
        assertEquals(1_580, status.remainingSteps);
        assertFalse(status.goalReached);
    }

    @Test
    public void reachingTargetCompletesTheGoal() {
        TodayStepStatus status = new TodayStepStatus(5_000, 5_000);

        assertEquals(5_000, status.progressSteps);
        assertEquals(0, status.remainingSteps);
        assertTrue(status.goalReached);
    }

    @Test
    public void progressIsCappedWhileActualTotalRemainsVisible() {
        TodayStepStatus status = new TodayStepStatus(6_250, 5_000);

        assertEquals(6_250, status.totalSteps);
        assertEquals(5_000, status.progressSteps);
        assertEquals(0, status.remainingSteps);
        assertTrue(status.goalReached);
    }

    @Test
    public void reserveAvailabilityIsProjectedWithoutSpendingItToday() {
        TodayStepStatus status = new TodayStepStatus(4_000, 5_000, 1_500);

        assertTrue(status.reserveSufficient);
        assertEquals(1_500, status.reserveSteps);
        assertEquals(1_500, status.projectedReserveSteps);
    }

    @Test
    public void surplusFillsReserveOnlyUpToTheDailyGoal() {
        TodayStepStatus status = new TodayStepStatus(10_000, 5_000, 1_500);

        assertEquals(5_000, status.projectedReserveSteps);
        assertEquals(3_500, status.projectedReserveAdded);
    }
}
