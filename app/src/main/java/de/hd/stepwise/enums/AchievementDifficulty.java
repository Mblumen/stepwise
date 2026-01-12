package de.hd.stepwise.enums;

public enum AchievementDifficulty {
    STONE("stone",0),
    BRONZE("bronze",1),
    SILVER("silver", 2),
    GOLD("gold", 3);

    private final int key;

    AchievementDifficulty(String name, int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static AchievementDifficulty fromString(String name) {
        for (AchievementDifficulty difficulty : values()) {
            if (difficulty.name().equalsIgnoreCase(name)) return difficulty;
        }
        return STONE;
    }
    public static AchievementDifficulty fromKey(int key) {
        for (AchievementDifficulty difficulty : values()) {
            if (difficulty.key == key) return difficulty;
        }
        return STONE;
    }
}
