package de.hd.stepwise.progresstracking;

import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;
import de.hd.stepwise.repositories.UserSettingsRepository;

@Singleton
public class StepSourceManager {

    private final UserSettingsRepository userSettingsRepository;
    private final FitbitSource fitbitSource;
    private final SensorSource sensorSource;

    interface FitbitSource {
        void initialize(Consumer<Boolean> callback);
    }

    interface SensorSource {
        boolean start();
        void stop();
    }

    @Inject
    public StepSourceManager(UserSettingsRepository userSettingsRepository, FitbitSyncStateManager fitbitSyncStateManager, StepSensorManager stepSensorManager) {
        this(userSettingsRepository, fitbitSyncStateManager::startStepTracking, new SensorSource() {
            @Override
            public boolean start() {
                return stepSensorManager.start();
            }

            @Override
            public void stop() {
                stepSensorManager.stop();
            }
        });
    }

    StepSourceManager(UserSettingsRepository userSettingsRepository, FitbitSource fitbitSource,
                      SensorSource sensorSource) {
        this.userSettingsRepository = userSettingsRepository;
        this.fitbitSource = fitbitSource;
        this.sensorSource = sensorSource;
    }

    public void setStepSource(StepSource newSource, Consumer<MethodResult> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            StepSource current = userSettingsRepository.getStepSourceSync();
            if (current == newSource) {
                report(callback, ResultStatus.SUCCESS, "Step source unchanged");
                return;
            }
            Runnable combinedCallback = () -> {
                userSettingsRepository.updateStepSource(newSource);
                report(callback, ResultStatus.SUCCESS, "Step source updated");
            };
            switch (newSource) {
                case STEP_COUNTER:
                    switchToSensor(combinedCallback, callback);
                    break;
                case FITBIT:
                    switchToFitbit(combinedCallback, callback);
                    break;
            }
        });
    }

    private void switchToSensor(Runnable callback, Consumer<MethodResult> resultCallback) {
        if (sensorSource.start()) {
            callback.run();
        } else {
            report(resultCallback, ResultStatus.ERROR, "Phone step counter is unavailable");
        }
    }

    private void switchToFitbit(Runnable callback, Consumer<MethodResult> resultCallback) {
        fitbitSource.initialize(success -> {
            if (!success) {
                report(resultCallback, ResultStatus.ERROR, "Could not initialize Google Health step tracking");
                return;
            }
            sensorSource.stop();
            callback.run();
        });
    }

    private void report(Consumer<MethodResult> callback, ResultStatus status, String message) {
        if (callback != null) callback.accept(new MethodResult(status, message));
    }
}
