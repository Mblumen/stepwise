package de.hd.stepwise;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import de.hd.stepwise.progresstracking.NotificationHandler;
import de.hd.stepwise.repositories.AchievementProgressReconciler;

@HiltAndroidApp
public class StepwiseApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @Inject
    AchievementProgressReconciler achievementProgressReconciler;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHandler.createNotificationChannel(this);
        Executors.newSingleThreadExecutor().execute(achievementProgressReconciler::reconcileSilently);
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}
