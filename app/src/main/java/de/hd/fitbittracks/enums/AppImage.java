package de.hd.fitbittracks.enums;

import de.hd.fitbittracks.R;

public enum AppImage {
    BARCELONA("barcelona", R.drawable.barcelona),
    PARIS("paris", R.drawable.paris),
    BERLIN("berlin", R.drawable.berlin),
    MUNICH("munich", R.drawable.munich),
    LOCK("lock", R.drawable.lock),
    STEPS("steps", R.drawable.steps);

    public final String key;
    public final int resId;

    AppImage(String key, int resId) {
        this.key = key;
        this.resId = resId;
    }

    public static int getResIdFor(String key) {
        for (AppImage img : values()) {
            if (img.key.equals(key)) return img.resId;
        }
        return R.drawable.avatar_1;
    }
}