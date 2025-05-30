package de.hd.fitbittracks.converter;

import androidx.room.TypeConverter;

import de.hd.fitbittracks.enums.ProgressStatus;

public class Converters {
    @TypeConverter
    public static ProgressStatus fromString(String value) {
        return value == null ? ProgressStatus.UNDEFINED : ProgressStatus.getFromKey(value);
    }

    @TypeConverter
    public static String toString(ProgressStatus status) {
        return status.key;
    }
}
