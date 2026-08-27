package de.hd.stepwise.progresstracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.pojos.MethodResult;

public class StepSourceManagerTest {

    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    @Test
    public void switchingFromGoogleHealthStartsSensorBeforePersistingSmartphoneSource() {
        FakeSettingsStore settings = new FakeSettingsStore(StepSource.FITBIT);
        AtomicBoolean sensorStarted = new AtomicBoolean();
        AtomicReference<MethodResult> result = new AtomicReference<>();
        StepSourceManager manager = manager(settings, () -> {
            sensorStarted.set(true);
            return true;
        });

        manager.setStepSource(StepSource.STEP_COUNTER, result::set);

        assertTrue(sensorStarted.get());
        assertEquals(StepSource.STEP_COUNTER, settings.source);
        assertEquals(ResultStatus.SUCCESS, result.get().status);
    }

    @Test
    public void unavailableSensorDoesNotPersistSmartphoneSource() {
        FakeSettingsStore settings = new FakeSettingsStore(StepSource.FITBIT);
        AtomicReference<MethodResult> result = new AtomicReference<>();
        StepSourceManager manager = manager(settings, () -> false);

        manager.setStepSource(StepSource.STEP_COUNTER, result::set);

        assertEquals(StepSource.FITBIT, settings.source);
        assertEquals(ResultStatus.ERROR, result.get().status);
        assertEquals("Phone step counter is unavailable", result.get().message);
    }

    @Test
    public void alreadySelectedSmartphoneSourceIsNotRestartedOrChanged() {
        FakeSettingsStore settings = new FakeSettingsStore(StepSource.STEP_COUNTER);
        AtomicBoolean sensorStarted = new AtomicBoolean();
        AtomicReference<MethodResult> result = new AtomicReference<>();
        StepSourceManager manager = manager(settings, () -> {
            sensorStarted.set(true);
            return true;
        });

        manager.setStepSource(StepSource.STEP_COUNTER, result::set);

        assertFalse(sensorStarted.get());
        assertEquals(0, settings.updateCount);
        assertEquals(StepSource.STEP_COUNTER, settings.source);
        assertEquals(ResultStatus.SUCCESS, result.get().status);
    }

    private static StepSourceManager manager(StepSourceManager.SettingsStore settings,
                                             BooleanSupplier sensorStart) {
        StepSourceManager.SensorSource sensor = new StepSourceManager.SensorSource() {
            @Override
            public boolean start() {
                return sensorStart.getAsBoolean();
            }

            @Override
            public void stop() {
            }
        };
        return new StepSourceManager(settings, callback -> callback.accept(true),
                sensor, DIRECT_EXECUTOR);
    }

    private static class FakeSettingsStore implements StepSourceManager.SettingsStore {
        private StepSource source;
        private int updateCount;

        FakeSettingsStore(StepSource source) {
            this.source = source;
        }

        @Override
        public StepSource getStepSource() {
            return source;
        }

        @Override
        public void updateStepSource(StepSource stepSource) {
            source = stepSource;
            updateCount++;
        }
    }
}
