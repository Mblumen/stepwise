package de.hd.stepwise.progresstracking;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;
import de.hd.stepwise.repositories.UserSettingsRepository;

@Singleton
public class StepSourceManager {

    private final UserSettingsRepository userSettingsRepository;
    private final FitbitSyncStateManager fitbitSyncStateManager;
    private final StepSensorManager stepSensorManager;
    @Inject
    public StepSourceManager(UserSettingsRepository userSettingsRepository, FitbitSyncStateManager fitbitSyncStateManager, StepSensorManager stepSensorManager) {
        this.userSettingsRepository = userSettingsRepository;
        this.fitbitSyncStateManager = fitbitSyncStateManager;
        this.stepSensorManager = stepSensorManager;;
    }

    public void setStepSource(StepSource newSource, Runnable callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            StepSource current = userSettingsRepository.getStepSourceSync();
            if (current == newSource) return;
            Runnable combinedCallback = () -> {
                userSettingsRepository.updateStepSource(newSource);
                if(callback != null) callback.run();
            };
            switch (newSource) {
                case STEP_COUNTER -> switchToSensor(combinedCallback);
                case FITBIT -> switchToFitbit(combinedCallback);
            }
        });
    }

    private void switchToSensor(Runnable callback) {
        stepSensorManager.start();
        if(callback != null) callback.run();
    }

    private void switchToFitbit(Runnable callback) {
        stepSensorManager.stop();
        fitbitSyncStateManager.startStepTracking(callback);
    }
}