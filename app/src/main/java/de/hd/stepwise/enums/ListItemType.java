package de.hd.stepwise.enums;

public enum ListItemType {
    ELEMENT(1),
    SEPARATOR(2),
    UNDEFINED(-1);

    public final int key;

    ListItemType(int key) {
        this.key = key;
    }

    public static ListItemType getFromKey(int key) {
        for (ListItemType status : values()) {
            if (status.key == key) return status;
        }
        return UNDEFINED;
    }
}
