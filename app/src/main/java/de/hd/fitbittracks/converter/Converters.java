package de.hd.fitbittracks.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import de.hd.fitbittracks.enums.AchievementDifficulty;
import de.hd.fitbittracks.enums.AchievementType;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.enums.RecordType;
import de.hd.fitbittracks.pojos.MilestoneImage;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static ProgressStatus fromString(String value) {
        return value == null ? ProgressStatus.UNDEFINED : ProgressStatus.getFromKey(value);
    }

    @TypeConverter
    public static String toString(ProgressStatus status) {
        return status.key;
    }

    @TypeConverter
    public static List<MilestoneImage> fromJson(String value) {
        if (value == null) return Collections.emptyList();
        Type listType = new TypeToken<List<MilestoneImage>>() {
        }.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toJson(List<MilestoneImage> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static AchievementDifficulty fromKey(int key) {
        return AchievementDifficulty.getFromKey(key);
    }

    @TypeConverter
    public static int toKey(AchievementDifficulty difficulty) {
        return difficulty.getKey();
    }

    @TypeConverter
    public static AchievementType fromOrder(int key) {
        return AchievementType.fromOrder(key);
    }

    @TypeConverter
    public static int toKey(AchievementType type) {
        return type.order;
    }

    @TypeConverter
    public static RecordType fromRecordKey(String key) {
        return RecordType.fromString(key);
    }

    @TypeConverter
    public static String fromRecord(RecordType type) {
        return type.displayName;
    }
}
