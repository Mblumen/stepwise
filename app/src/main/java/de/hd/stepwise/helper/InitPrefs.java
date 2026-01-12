package de.hd.stepwise.helper;

import android.content.Context;

public final class InitPrefs {

    private static final String PREFS = "app_init_prefs";
    private static final String KEY_DONE = "data_initialized";

    public static boolean isInitialized(Context context) {
        return context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DONE, false);
    }

    public static void markInitialized(Context context) {
        context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DONE, true)
                .apply();
    }
}