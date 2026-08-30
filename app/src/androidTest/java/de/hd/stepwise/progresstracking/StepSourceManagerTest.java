package de.hd.stepwise.progresstracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.repositories.UserSettingsRepository;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.enums.ResultStatus;

@RunWith(AndroidJUnit4.class)
public class StepSourceManagerTest {
    private AppDatabase database;
    private UserSettingsRepository settingsRepository;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        UserSettings settings = new UserSettings();
        settings.stepSource = StepSource.STEP_COUNTER;
        database.userSettingsDao().insertOrUpdate(settings);
        settingsRepository = new UserSettingsRepository(database);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void failedFitbitInitializationKeepsSensorSelectedAndRunning() throws Exception {
        FakeSensor sensor = new FakeSensor();
        StepSourceManager manager = new StepSourceManager(
                settingsStore(),
                callback -> callback.accept(false),
                sensor,
                Runnable::run
        );

        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<MethodResult> result = new AtomicReference<>();
        manager.setStepSource(StepSource.FITBIT, value -> {
            result.set(value);
            completed.countDown();
        });

        assertTrue(completed.await(2, TimeUnit.SECONDS));
        assertEquals(ResultStatus.ERROR, result.get().status);
        assertEquals(StepSource.STEP_COUNTER, settingsRepository.getStepSourceSync());
        assertFalse(sensor.stopped.get());
    }

    @Test
    public void successfulFitbitInitializationStopsSensorBeforePublishingSelection() throws Exception {
        FakeSensor sensor = new FakeSensor();
        StepSourceManager manager = new StepSourceManager(
                settingsStore(),
                callback -> callback.accept(true),
                sensor,
                Runnable::run
        );
        CountDownLatch switched = new CountDownLatch(1);

        manager.setStepSource(StepSource.FITBIT, result -> switched.countDown());

        assertTrue(switched.await(2, TimeUnit.SECONDS));
        assertTrue(sensor.stopped.get());
        assertEquals(StepSource.FITBIT, settingsRepository.getStepSourceSync());
    }

    private StepSourceManager.SettingsStore settingsStore() {
        return new StepSourceManager.SettingsStore() {
            @Override
            public StepSource getStepSource() {
                return settingsRepository.getStepSourceSync();
            }

            @Override
            public void updateStepSource(StepSource stepSource) {
                settingsRepository.updateStepSource(stepSource);
            }
        };
    }

    private static class FakeSensor implements StepSourceManager.SensorSource {
        final AtomicBoolean stopped = new AtomicBoolean();

        @Override
        public boolean start() {
            return true;
        }

        @Override
        public void stop() {
            stopped.set(true);
        }
    }
}
