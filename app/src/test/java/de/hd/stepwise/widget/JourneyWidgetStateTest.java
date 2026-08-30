package de.hd.stepwise.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.List;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.enums.ProgressStatus;
import de.hd.stepwise.pojos.JourneySummary;

public class JourneyWidgetStateTest {
    @Test
    public void activeJourneyMapsProgressAndNextMilestone() {
        MilestoneWithTotalDistance next = new MilestoneWithTotalDistance();
        next.title = "Summit";
        JourneyWidgetState state = JourneyWidgetState.from(summary(
                ProgressStatus.ACTIVE, 0.425f, next));

        assertEquals(JourneyWidgetState.Kind.ACTIVE, state.kind);
        assertEquals(43, state.progressPercent);
        assertEquals(1_250f, state.distanceWalked, 0f);
        assertEquals("Summit", state.nextMilestone);
        assertEquals(77L, state.progressId);
    }

    @Test
    public void pausedAndEmptyStatesAreSelectedDeliberately() {
        assertEquals(JourneyWidgetState.Kind.PAUSED,
                JourneyWidgetState.from(summary(ProgressStatus.PAUSED, 1f, null)).kind);
        assertEquals(JourneyWidgetState.Kind.EMPTY, JourneyWidgetState.from(null).kind);
        assertEquals(JourneyWidgetState.Kind.EMPTY,
                JourneyWidgetState.from(summary(ProgressStatus.COMPLETED, 1f, null)).kind);
    }

    @Test
    public void progressIsClampedToWidgetRange() {
        assertEquals(100, JourneyWidgetState.from(
                summary(ProgressStatus.ACTIVE, 1.5f, null)).progressPercent);
        assertEquals(0, JourneyWidgetState.from(
                summary(ProgressStatus.ACTIVE, -0.5f, null)).progressPercent);
    }

    @Test
    public void unavailableStateIsExplicit() {
        assertEquals(JourneyWidgetState.Kind.UNAVAILABLE,
                JourneyWidgetState.unavailable().kind);
    }

    @Test
    public void absentCachedImageRemainsAFallbackSignal() {
        assertNull(JourneyWidgetState.from(
                summary(ProgressStatus.ACTIVE, 0.5f, null)).imagePath);
    }

    private static JourneySummary summary(ProgressStatus status, float fraction,
                                          MilestoneWithTotalDistance next) {
        Track track = new Track();
        track.name = "Trail";
        return new JourneySummary(77, track, status, 2_000, 1_250f, 3_000,
                fraction, next, 1L, null, 1L, 1L, 0L, List.of());
    }
}
