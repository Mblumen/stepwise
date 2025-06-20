package de.hd.fitbittracks.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.hd.fitbittracks.enums.AchievementDifficulty;
import de.hd.fitbittracks.enums.AchievementType;

@Entity(tableName = "achievements")
public class Achievement {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String key; // Unique identifier for logic, e.g., "WALK_100KM"

    @NonNull
    public String title;

    @NonNull
    public String description;

    @NonNull
    public String icon; // icon file name or URI

    @NonNull
    public AchievementType type;

    @NonNull
    public AchievementDifficulty difficulty; // Difficulty level of the achievement

    public float targetValue;

    public float progressValue;

    public boolean unlocked;

    @Nullable
    public Long dateUnlocked;

    public Achievement(@NonNull String key, @NonNull String title, @NonNull String description,
                             @NonNull String icon, @NonNull AchievementType type, @NonNull AchievementDifficulty difficulty,
                             float targetValue, float progressValue, boolean unlocked,
                             @Nullable Long dateUnlocked) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.difficulty = difficulty;
        this.targetValue = targetValue;
        this.progressValue = progressValue;
        this.unlocked = unlocked;
        this.dateUnlocked = dateUnlocked;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Achievement)) return false;
        Achievement other = (Achievement) obj;
        return key.equals(other.key) &&
               title.equals(other.title) &&
               description.equals(other.description) &&
               icon.equals(other.icon) &&
               type == other.type &&
               targetValue == other.targetValue &&
               progressValue == other.progressValue &&
               unlocked == other.unlocked &&
               (dateUnlocked != null ? dateUnlocked.equals(other.dateUnlocked) : other.dateUnlocked == null);
    }
}