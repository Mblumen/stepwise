package de.hd.stepwise.enums;


import de.hd.stepwise.R;

public enum RecordType {
    DISTANCE("Distance", R.drawable.distance),          // e.g., total distance walked
    STEPS("Steps", R.drawable.steps),
    DURATION("Duration", R.drawable.timer), // e.g., total time spent walking
    TRACK("Track", R.drawable.map); // e.g., complete 5 tracks// e.g., 7-day walk streak

    public final String displayName;
    public final int iconResId;

    RecordType(String displayName, int iconResId) {
        this.displayName = displayName;
        this.iconResId = iconResId;
    }

    public static RecordType fromString(String type) {
        for (RecordType t : values()) {
            if (t.displayName.equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown RecordType: " + type);
    }
}