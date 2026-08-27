package de.hd.stepwise.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.ReachedMilestone;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.entities.UserProgress;
import de.hd.stepwise.enums.ProgressStatus;
import de.hd.stepwise.pojos.JourneySummary;
import de.hd.stepwise.pojos.TrackWithMilestones;
import de.hd.stepwise.pojos.UserProgressWithTrackAndMilestones;

public class JourneySummaryRepositoryTest {

    @Test
    public void journeySummaryCalculatesTotalsNextMilestoneTimingAndChronologicalHistory() {
        UserProgressWithTrackAndMilestones journey = journey();
        JourneySummary summary = JourneySummaryRepository.summarize(journey);

        assertEquals(7L, summary.progressId);
        assertEquals("Test Track", summary.track.name);
        assertEquals(ProgressStatus.COMPLETED, summary.status);
        assertEquals(1200f, summary.distanceWalked, 0f);
        assertEquals(2500, summary.totalDistance);
        assertEquals(0.48f, summary.progressFraction, 0.001f);
        assertEquals(22L, summary.nextMilestone.id);
        assertEquals(8000L, summary.totalDuration.longValue());
        assertEquals(7000L, summary.activeDuration.longValue());
        assertEquals(2, summary.reachedMilestones.size());
        assertEquals(11L, summary.reachedMilestones.get(0).milestone.id);
        assertEquals(22L, summary.reachedMilestones.get(1).milestone.id);
    }

    @Test
    public void missingJourneyReturnsNull() {
        assertNull(JourneySummaryRepository.summarize(null));
    }

    @Test
    public void pausedJourneyIncludesElapsedAndCurrentPauseTime() {
        UserProgressWithTrackAndMilestones journey = journey();
        journey.userProgress.status = ProgressStatus.PAUSED;
        journey.userProgress.completedAt = null;
        journey.userProgress.startedAt = 1000L;
        journey.userProgress.totalPausedTime = 500L;
        journey.userProgress.pausedAt = 7000L;

        JourneySummary summary = JourneySummaryRepository.summarize(journey, 9000L);

        assertEquals(8000L, summary.totalDuration.longValue());
        assertEquals(2500L, summary.pausedDuration.longValue());
        assertEquals(5500L, summary.activeDuration.longValue());
    }

    private static UserProgressWithTrackAndMilestones journey() {
        UserProgress progress = new UserProgress();
        progress.id = 7;
        progress.trackId = 3;
        progress.stepsWalked = 1600;
        progress.distanceWalked = 1200;
        progress.status = ProgressStatus.COMPLETED;
        progress.startedAt = 1000L;
        progress.completedAt = 9000L;
        progress.totalPausedTime = 1000L;

        Track track = new Track();
        track.id = 3;
        track.name = "Test Track";
        track.startLocation = "Start";
        track.endLocation = "End";

        MilestoneWithTotalDistance first = milestone(11, 1000);
        MilestoneWithTotalDistance second = milestone(22, 2500);
        TrackWithMilestones trackWithMilestones = new TrackWithMilestones();
        trackWithMilestones.track = track;
        trackWithMilestones.milestones = List.of(first, second);

        ReachedMilestone reachedSecond = new ReachedMilestone(7, 22, 1600, 8000);
        ReachedMilestone reachedFirst = new ReachedMilestone(7, 11, 1300, 4000);

        UserProgressWithTrackAndMilestones result = new UserProgressWithTrackAndMilestones();
        result.userProgress = progress;
        result.trackWithMilestones = trackWithMilestones;
        result.reachedMilestones = new ArrayList<>(List.of(reachedSecond, reachedFirst));
        return result;
    }

    private static MilestoneWithTotalDistance milestone(long id, int totalDistance) {
        MilestoneWithTotalDistance milestone = new MilestoneWithTotalDistance();
        milestone.id = id;
        milestone.trackId = 3;
        milestone.title = "Milestone " + id;
        milestone.description = "Description";
        milestone.totalDistance = totalDistance;
        return milestone;
    }
}
