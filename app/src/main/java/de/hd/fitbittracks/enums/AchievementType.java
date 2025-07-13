package de.hd.fitbittracks.enums;

public enum AchievementType {
    DISTANCE("Distance", 0),          // e.g., total distance walked
    STEPS("Steps", 1),
    MILESTONES_REACHED("Milestones Reached", 2), // e.g., reach X milestones// e.g., number of steps in a day
    TRACKS_COMPLETED("Tracks Completed", 3),  // e.g., complete 5 tracks
    STREAK_DAYS("Streaks", 4);      // e.g., 7-day walk streak

    public final String displayName;
    public final int order;

    AchievementType(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }

    public static AchievementType fromString(String type) {
        for (AchievementType t : values()) {
            if (t.displayName.equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown AchievementType: " + type);
    }

    public static AchievementType fromOrder(int order) {
        for (AchievementType t : values()) {
            if (t.order == order) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown AchievementType order: " + order);
    }
}