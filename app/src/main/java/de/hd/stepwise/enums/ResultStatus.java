package de.hd.stepwise.enums;

public enum ResultStatus {
    SUCCESS("success"),
    ERROR("error"),
    WARNING("warning");

    public final String key;

    ResultStatus(String key) {
        this.key = key;
    }

    public static ResultStatus getFromKey(String key) {
        for (ResultStatus status : values()) {
            if (status.key.equals(key)) return status;
        }
        return ERROR;
    }
}
