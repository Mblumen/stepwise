package de.hd.fitbittracks.enums;

import de.hd.fitbittracks.R;

public enum AchievementDifficulty {
    STONE(0),
    BRONZE(1),
    SILVER(2),
    GOLD(3);

    private final int key;

    AchievementDifficulty(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static AchievementDifficulty getFromKey(int key) {
        for (AchievementDifficulty difficulty : values()) {
            if (difficulty.key == key) return difficulty;
        }
        return STONE;
    }
}
