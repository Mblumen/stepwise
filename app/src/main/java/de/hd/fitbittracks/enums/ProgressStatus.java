package de.hd.fitbittracks.enums;

import de.hd.fitbittracks.R;

public enum ProgressStatus {
    ACTIVE("active"),
    PAUSED("paused"),
    COMPLETED("completed"),
    UNDEFINED("undefined");

    public final String key;

    ProgressStatus(String key) {
        this.key = key;
    }

    public static ProgressStatus getFromKey(String key) {
        for (ProgressStatus status : values()) {
            if (status.key.equals(key)) return status;
        }
        return UNDEFINED;
    }
}
