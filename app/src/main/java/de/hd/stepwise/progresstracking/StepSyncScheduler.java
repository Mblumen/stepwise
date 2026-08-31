package de.hd.stepwise.progresstracking;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.lifecycle.LiveData;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;
import de.hd.stepwise.repositories.UserSettingsRepository;


@Singleton
public class StepSyncScheduler {

    private final Context context;
    private final UserSettingsRepository userSettingsRepository;
    public static final String WORK_TAG = "fitbit_sync_chain";
    private static final String MANUAL_WORK_NAME = "manual_step_sync";

    @Inject
    public StepSyncScheduler(@ApplicationContext Context context, UserSettingsRepository userSettingsRepository) {
        this.context = context;
        this.userSettingsRepository = userSettingsRepository;
    }

    public void startWorker() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(StepSyncWorker.class, 15, TimeUnit.MINUTES)
                        .setInitialDelay(0, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .addTag(WORK_TAG)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, work);
    }

    public void stopWorker() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
    }

    public LiveData<WorkInfo> triggerManualSync() {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(StepSyncWorker.class)
                .setInputData(new androidx.work.Data.Builder()
                        .putBoolean(StepSyncWorker.INPUT_MANUAL_SYNC, true)
                        .build())
                .build();
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniqueWork(MANUAL_WORK_NAME, ExistingWorkPolicy.REPLACE, work);
        return workManager.getWorkInfoByIdLiveData(work.getId());
    }

    public void scheduleNextRun() {
        int delay = userSettingsRepository.getRefreshTimeInMinutesFitbit();
        WorkManager workManager = WorkManager.getInstance(context);
        OneTimeWorkRequest work =
                new OneTimeWorkRequest.Builder(StepSyncWorker.class)
                        .setInitialDelay(delay, TimeUnit.MINUTES)
                        .addTag(WORK_TAG)
                        .build();

        workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, work);

    }

/*    public void triggerImmediateSync() {
        Log.d("StepSyncWorker", "Triggering immediate sync...");
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(UNIQUE_WORK_NAME);
        WorkManager.getInstance(context).enqueue(new OneTimeWorkRequest.Builder(StepSyncWorker.class)
                .addTag(UNIQUE_WORK_NAME)
                .build());
                *//*.enqueueUniqueWork(
                        UNIQUE_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        new OneTimeWorkRequest.Builder(StepSyncWorker.class).build()
                );*//*
    }*/
}
