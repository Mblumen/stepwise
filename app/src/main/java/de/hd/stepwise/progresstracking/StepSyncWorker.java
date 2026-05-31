package de.hd.stepwise.progresstracking;

import static de.hd.stepwise.enums.StepSource.FITBIT;
import static de.hd.stepwise.enums.StepSource.STEP_COUNTER;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import de.hd.stepwise.entities.StepEvent;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;
import de.hd.stepwise.pojos.events.StepUpdateResult;
import de.hd.stepwise.repositories.StepEventRepository;
import de.hd.stepwise.repositories.UserProgressRepository;
import de.hd.stepwise.repositories.UserSettingsRepository;

@HiltWorker
public class StepSyncWorker extends Worker {

    private final FitbitSyncStateManager fitbitSyncStateManager;
    private final UserSettingsRepository userSettingsRepository;
    private final UserProgressRepository userProgressRepository;
    private final StepEventRepository stepEventRepository;
    private final NotificationHandler notificationHandler;
    private final StepSyncScheduler stepSyncScheduler;

    @AssistedInject
    public StepSyncWorker(
            @Assisted Context context,
            @Assisted WorkerParameters params,
            FitbitSyncStateManager fitbitSyncStateManager,
            UserSettingsRepository userSettingsRepository,
            UserProgressRepository userProgressRepository, StepEventRepository stepEventRepository,
            NotificationHandler notificationHandler, StepSyncScheduler stepSyncScheduler
    ) {
        super(context, params);
        this.fitbitSyncStateManager = fitbitSyncStateManager;
        this.userSettingsRepository = userSettingsRepository;
        this.userProgressRepository = userProgressRepository;
        this.stepEventRepository = stepEventRepository;
        this.notificationHandler = notificationHandler;
        this.stepSyncScheduler = stepSyncScheduler;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d("StepSyncWorker", "Running steps sync...");
            StepSource currentStepSource = userSettingsRepository.getStepSourceSync();
            if (currentStepSource == FITBIT) {
                syncFromFitbit();
            } else if (currentStepSource == STEP_COUNTER) {
                syncFromSensor();
            }

            //scheduleNextRun(); //
            return Result.success();
        } catch (Exception e) {
            Log.e("FitbitSyncWorker", "Sync failed", e);
            return Result.retry();
        } finally {
            //stepSyncScheduler.scheduleNextRun();
        }
    }

    private void syncFromFitbit() {
        Log.d("StepSyncWorker", "Syncing steps from Fitbit...");
        List<FitbitSyncStateManager.DailyStepRecord> apiResponse = fitbitSyncStateManager.getStepDataPastWeekSync();
        if (apiResponse != null && !apiResponse.isEmpty()) {
            fitbitSyncStateManager.save(new FitbitSyncStateManager.FitbitSyncState(apiResponse), false);
            List<StepEvent> stepEvents = stepEventRepository.getUnhandledStepEvents(FITBIT);
            AtomicInteger totalSteps = new AtomicInteger();
            stepEvents.forEach(se -> {
                totalSteps.addAndGet(se.steps);
                stepEventRepository.markEventHandled(se.id);
            });
            StepUpdateResult stepUpdateResult = userProgressRepository.updateStepsWalked(totalSteps.get());
            notificationHandler.handleStepUpdate(stepUpdateResult);
        } else {
            Log.d("StepSyncWorker", "No data received from Fitbit");
        }
    }

   /* private void readFromSensor() {
        Log.d("StepSyncWorker", "Reading steps from sensor...");
        SensorManager sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            Log.e("StepSyncWorker", "SensorManager is null");
            return;
        }

        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null) {
            Log.e("StepSyncWorker", "Step sensor not available");
            return;
        }

        final Object lock = new Object();
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                int totalSteps = (int) event.values[0];
                handleSensorSteps(totalSteps);
                sensorManager.unregisterListener(this);
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        synchronized (lock) {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

            try {
                // Wait for sensor callback (max ~2 seconds)
                lock.wait(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            sensorManager.unregisterListener(listener);
        }
    }*/

/*    private void handleSensorSteps(int totalSteps) {
        int lastValue = getLastSensorValue(getApplicationContext());
        Log.d("StepSyncWorker", "Sensor value: " + totalSteps + ", last: " + lastValue);

        if (lastValue < 0) {
            // First run → just store baseline
            setLastSensorValue(getApplicationContext(), totalSteps);
            Log.d("StepSyncWorker", "Initial sensor baseline set");
        } else if (totalSteps < lastValue) {
            // Device reboot or sensor reset
            Log.w("StepSyncWorker", "Sensor reset detected");
            setLastSensorValue(getApplicationContext(), totalSteps);
        } else {
            int stepsDelta = totalSteps - lastValue;

            if (stepsDelta > 0) {
                Log.d("StepSyncWorker", "Steps delta: " + stepsDelta);
                StepUpdateResult stepUpdateResult = userProgressRepository.updateStepsWalked(stepsDelta);
                notificationHandler.handleStepUpdate(stepUpdateResult);
            }

            setLastSensorValue(getApplicationContext(), totalSteps);
        }
    }*/

    private void syncFromSensor() {
        Log.d("StepSyncWorker", "Syncing steps from sensor...");
        List<StepEvent> stepEvents = stepEventRepository.getUnhandledStepEvents(STEP_COUNTER);
        AtomicInteger totalSteps = new AtomicInteger();
        stepEvents.forEach(se -> {
            totalSteps.addAndGet(se.steps);
            stepEventRepository.markEventHandled(se.id);
        });
        if (totalSteps.get() > 0) {
            StepUpdateResult stepUpdateResult = userProgressRepository.updateStepsWalked(totalSteps.get());
            notificationHandler.handleStepUpdate(stepUpdateResult);
        } else {
            Log.d("StepSyncWorker", "No new step events from sensor");
        }
    }
}