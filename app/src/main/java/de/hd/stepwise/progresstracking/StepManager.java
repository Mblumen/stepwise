package de.hd.stepwise.progresstracking;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.repositories.UserSettingsRepository;

@Singleton
public class StepManager {

    private final StepSensorManager stepSensorManager;
    private final UserSettingsRepository userSettingsRepository;
    private final StepSyncScheduler stepSyncScheduler;
    private boolean initialized = false;

    @Inject
    public StepManager(StepSensorManager stepSensorManager, UserSettingsRepository userSettingsRepository, StepSyncScheduler stepSyncScheduler) {
        this.stepSyncScheduler = stepSyncScheduler;
        this.stepSensorManager = stepSensorManager;
        this.userSettingsRepository = userSettingsRepository;
    }

    public void initialize() {
        if (initialized) return;
        initialized = true;
        Executors.newSingleThreadExecutor().execute(() -> {
            if (userSettingsRepository.getStepSourceSync() == StepSource.STEP_COUNTER) {
                stepSensorManager.start();
            } else {
                stepSensorManager.stop();
            }
            stepSyncScheduler.startWorker();
        });
    }

    public void stop() {
        stepSensorManager.stop();
        stepSyncScheduler.stopWorker();
    }

}
