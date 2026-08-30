package de.hd.stepwise.widget;

import de.hd.stepwise.enums.ProgressStatus;
import de.hd.stepwise.pojos.JourneySummary;

public class JourneyWidgetState {
    public enum Kind { ACTIVE, PAUSED, EMPTY, UNAVAILABLE }

    public final Kind kind;
    public final long progressId;
    public final String trackName;
    public final String imagePath;
    public final int progressPercent;
    public final float distanceWalked;
    public final String nextMilestone;

    private JourneyWidgetState(Kind kind, long progressId, String trackName, String imagePath,
                               int progressPercent, float distanceWalked, String nextMilestone) {
        this.kind = kind;
        this.progressId = progressId;
        this.trackName = trackName;
        this.imagePath = imagePath;
        this.progressPercent = progressPercent;
        this.distanceWalked = distanceWalked;
        this.nextMilestone = nextMilestone;
    }

    public static JourneyWidgetState from(JourneySummary summary) {
        if (summary == null) return empty();
        Kind kind = summary.status == ProgressStatus.ACTIVE ? Kind.ACTIVE
                : summary.status == ProgressStatus.PAUSED ? Kind.PAUSED : Kind.EMPTY;
        if (kind == Kind.EMPTY) return empty();
        return new JourneyWidgetState(kind, summary.progressId,
                summary.track.name, summary.track.localImagePath,
                Math.max(0, Math.min(100, Math.round(summary.progressFraction * 100))),
                summary.distanceWalked,
                summary.nextMilestone == null ? null : summary.nextMilestone.title);
    }

    public static JourneyWidgetState empty() {
        return new JourneyWidgetState(Kind.EMPTY, -1, "", null, 0, 0, "");
    }

    public static JourneyWidgetState unavailable() {
        return new JourneyWidgetState(Kind.UNAVAILABLE, -1, "", null, 0, 0, "");
    }
}
