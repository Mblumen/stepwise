package de.hd.stepwise.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.UserProgressDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.ReachedMilestone;
import de.hd.stepwise.entities.UserProgress;
import de.hd.stepwise.pojos.JourneyReachedMilestone;
import de.hd.stepwise.pojos.JourneySummary;
import de.hd.stepwise.pojos.UserProgressWithTrackAndMilestones;

@Singleton
public class JourneySummaryRepository {

    private final UserProgressDao userProgressDao;

    @Inject
    public JourneySummaryRepository(AppDatabase database) {
        userProgressDao = database.userProgressDao();
    }

    public LiveData<JourneySummary> observeJourney(long progressId) {
        return Transformations.map(
                userProgressDao.observeProgressWithTrackAndMilestonesById(progressId),
                JourneySummaryRepository::summarize);
    }

    public LiveData<JourneySummary> observeCurrentJourney() {
        return Transformations.map(
                userProgressDao.observeCurrentProgressWithTrackAndMilestones(),
                JourneySummaryRepository::summarize);
    }

    public JourneySummary getCurrentJourneySync() {
        return summarize(userProgressDao.getCurrentProgressWithTrackAndMilestones());
    }

    static JourneySummary summarize(UserProgressWithTrackAndMilestones relation) {
        return summarize(relation, System.currentTimeMillis());
    }

    static JourneySummary summarize(UserProgressWithTrackAndMilestones relation, long now) {
        if (relation == null || relation.userProgress == null
                || relation.trackWithMilestones == null
                || relation.trackWithMilestones.track == null) {
            return null;
        }
        UserProgress progress = relation.userProgress;
        List<MilestoneWithTotalDistance> milestones = relation.trackWithMilestones.milestones == null
                ? List.of() : relation.trackWithMilestones.milestones;
        int totalDistance = milestones.stream()
                .mapToInt(milestone -> milestone.totalDistance)
                .max()
                .orElse(0);
        MilestoneWithTotalDistance nextMilestone = milestones.stream()
                .filter(milestone -> milestone.totalDistance > progress.distanceWalked)
                .min(Comparator.comparingInt(milestone -> milestone.totalDistance))
                .orElse(null);
        float progressFraction = totalDistance == 0 ? 0f
                : Math.max(0f, Math.min(1f, progress.distanceWalked / totalDistance));

        Map<Long, MilestoneWithTotalDistance> milestonesById = new HashMap<>();
        for (MilestoneWithTotalDistance milestone : milestones) {
            milestonesById.put(milestone.id, milestone);
        }
        List<JourneyReachedMilestone> reached = new ArrayList<>();
        if (relation.reachedMilestones != null) {
            for (ReachedMilestone reachedMilestone : relation.reachedMilestones) {
                MilestoneWithTotalDistance milestone = milestonesById.get(reachedMilestone.milestoneId);
                if (milestone != null) {
                    reached.add(new JourneyReachedMilestone(milestone, reachedMilestone));
                }
            }
        }
        reached.sort(Comparator.comparingLong(item -> item.reached.reachedAt));

        Long totalDuration = null;
        Long pausedDuration = null;
        Long activeDuration = null;
        if (progress.startedAt != null) {
            long end = progress.completedAt == null ? now : progress.completedAt;
            totalDuration = Math.max(0L, end - progress.startedAt);
            long persistedPaused = progress.totalPausedTime == null ? 0L : progress.totalPausedTime;
            long currentPause = progress.completedAt == null && progress.pausedAt != null
                    ? Math.max(0L, now - progress.pausedAt) : 0L;
            pausedDuration = persistedPaused + currentPause;
            activeDuration = Math.max(0L, totalDuration - pausedDuration);
        }

        return new JourneySummary(progress.id, relation.trackWithMilestones.track, progress.status,
                progress.stepsWalked, progress.distanceWalked, totalDistance, progressFraction,
                nextMilestone, progress.startedAt, progress.completedAt, totalDuration,
                activeDuration, pausedDuration, List.copyOf(reached));
    }
}
