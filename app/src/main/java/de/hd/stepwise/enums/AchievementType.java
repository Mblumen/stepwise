package de.hd.stepwise.enums;

public enum AchievementType {
    DISTANCE("distance", "Distance",0),          // e.g., total distance walked
    STEPS("steps", "Steps", 1),
    MILESTONES_REACHED("milestones_reached", "Milestones Reached", 2), // e.g., reach X milestones// e.g., number of steps in a day
    TRACKS_COMPLETED("tracks_completed", "Tracks Completed", 3),  // e.g., complete 5 tracks
    STREAK_DAYS("streaks", "Streaks", 4);      // e.g., 7-day walk streak

    public final String key;
    public final String displayName;
    public final int order;

    AchievementType(String key, String displayName, int order) {
        this.key = key;
        this.displayName = displayName;
        this.order = order;
    }

    public static AchievementType fromString(String type) {
        for (AchievementType t : values()) {
            if (t.key.equalsIgnoreCase(type)) {
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