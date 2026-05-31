package de.hd.stepwise.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import de.hd.stepwise.enums.AchievementDifficulty;
import de.hd.stepwise.enums.AchievementType;
import de.hd.stepwise.enums.ProgressStatus;
import de.hd.stepwise.enums.RecordType;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.pojos.MilestoneImage;
import de.hd.stepwise.pojos.TrackRoute;

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
        return AchievementDifficulty.fromKey(key);
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

    @TypeConverter
    public static TrackRoute fromTrackRouteJson(String value) {
        if (value == null) return null;
        return gson.fromJson(value, new TypeToken<TrackRoute>(){}.getType());
    }

    @TypeConverter
    public static String toTrackRouteJson(TrackRoute trackRoute) {
        return trackRoute == null ? null : gson.toJson(trackRoute);
    }

    @TypeConverter
    public static StepSource fromSensorKey(int key) {
        return StepSource.getFromKey(key);
    }

    @TypeConverter
    public static int toSensorKey(StepSource type) {
        return type.key;
    }
}
