package de.hd.stepwise.pojos;

import java.util.List;

import de.hd.stepwise.entities.Track;
import de.hd.stepwise.enums.ProgressStatus;

public class JourneyPassport {
    public final long progressId;
    public final Track track;
    public final int stepsWalked;
    public final float distanceWalked;
    public final Long startedAt;
    public final Long completedAt;
    public final Long totalDuration;
    public final Long activeDuration;
    public final List<JourneyReachedMilestone> reachedMilestones;

    private JourneyPassport(JourneySummary summary) {
        progressId = summary.progressId;
        track = summary.track;
        stepsWalked = summary.stepsWalked;
        distanceWalked = summary.distanceWalked;
        startedAt = summary.startedAt;
        completedAt = summary.completedAt;
        totalDuration = summary.totalDuration;
        activeDuration = summary.activeDuration;
        reachedMilestones = summary.reachedMilestones;
    }

    public static JourneyPassport from(JourneySummary summary) {
        if (summary == null || summary.status != ProgressStatus.COMPLETED) return null;
        return new JourneyPassport(summary);
    }

    public String displayTrackName(String fallback) { return fallback(track.name, fallback); }
    public String displayStart(String fallback) { return fallback(track.startLocation, fallback); }
    public String displayDestination(String fallback) { return fallback(track.endLocation, fallback); }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
