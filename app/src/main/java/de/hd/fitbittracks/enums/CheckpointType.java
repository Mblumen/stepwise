package de.hd.fitbittracks.enums;

public enum CheckpointType {
    MILESTONE("milestone"),
    FINISH("track"),;

    public final String string;

    CheckpointType(String string) {
        this.string = string;
    }

    public static CheckpointType getFromString(String string) {
        for (CheckpointType status : values()) {
            if (status.string == string) return status;
        }
        return MILESTONE;
    }
}
