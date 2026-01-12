package de.hd.stepwise;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class StepwiseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Any app-level initialization can go here
    }
}