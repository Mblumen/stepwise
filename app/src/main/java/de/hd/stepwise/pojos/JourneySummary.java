package de.hd.stepwise.pojos;

import java.util.List;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.enums.ProgressStatus;

public class JourneySummary {
    public final long progressId;
    public final Track track;
    public final ProgressStatus status;
    public final int stepsWalked;
    public final float distanceWalked;
    public final int totalDistance;
    public final float progressFraction;
    public final MilestoneWithTotalDistance nextMilestone;
    public final Long startedAt;
    public final Long completedAt;
    public final Long totalDuration;
    public final Long activeDuration;
    public final Long pausedDuration;
    public final List<JourneyReachedMilestone> reachedMilestones;

    public JourneySummary(long progressId, Track track, ProgressStatus status, int stepsWalked,
                          float distanceWalked, int totalDistance, float progressFraction,
                          MilestoneWithTotalDistance nextMilestone, Long startedAt,
                          Long completedAt, Long totalDuration, Long activeDuration,
                          Long pausedDuration, List<JourneyReachedMilestone> reachedMilestones) {
        this.progressId = progressId;
        this.track = track;
        this.status = status;
        this.stepsWalked = stepsWalked;
        this.distanceWalked = distanceWalked;
        this.totalDistance = totalDistance;
        this.progressFraction = progressFraction;
        this.nextMilestone = nextMilestone;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.totalDuration = totalDuration;
        this.activeDuration = activeDuration;
        this.pausedDuration = pausedDuration;
        this.reachedMilestones = reachedMilestones;
    }
}
