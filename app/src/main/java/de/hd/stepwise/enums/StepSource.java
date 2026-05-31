package de.hd.stepwise.enums;

public enum StepSource {
    STEP_COUNTER(0, "Smartphone"),
    FITBIT(1, "Fitbit");

    public final int key;
    public final String displayName;

    StepSource(int key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }
    public static StepSource getFromKey(int key) {
        for (StepSource stepSource : values()) {
            if (stepSource.key == key) return stepSource;
        }
        return STEP_COUNTER;
    }

    public static StepSource getFromDisplayName(String displayName) {
        for (StepSource stepSource : values()) {
            if (stepSource.displayName.equals(displayName)) return stepSource;
        }
        return STEP_COUNTER;
    }
}
