package de.hd.stepwise.repositories;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.List;

import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;
import de.hd.stepwise.pojos.StreakSummary;

@RunWith(AndroidJUnit4.class)
public class DailyActivityRepositoryTest {
    private AppDatabase database;
    private DailyActivityRepository repository;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        repository = new DailyActivityRepository(database);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void sameDaySourceContributionsCombineIntoOneQualifyingDay() {
        LocalDate date = LocalDate.of(2026, 8, 23);
        repository.recordSensorDelta(date, 3_000);
        repository.initializeFitbitBaselines(List.of(fitbit(date, 8_000)));
        repository.applyFitbitSnapshots(List.of(fitbit(date, 10_000)));

        StreakSummary summary = repository.getStreakSummary(date);

        assertEquals(5_000, repository.getTotalSteps(date));
        assertEquals(1, summary.currentDays);
        assertEquals(1, summary.longestDays);
        assertEquals(date, summary.currentStartDate);
        assertEquals(date, summary.longestStartDate);
        assertEquals(date, summary.longestEndDate);
    }

    @Test
    public void todayBelowThresholdKeepsStreakThroughYesterday() {
        LocalDate today = LocalDate.of(2026, 8, 23);
        repository.recordSensorDelta(today.minusDays(2), 5_000);
        repository.recordSensorDelta(today.minusDays(1), 5_000);
        repository.recordSensorDelta(today, 4_999);

        StreakSummary summary = repository.getStreakSummary(today);

        assertEquals(2, summary.currentDays);
        assertEquals(today.minusDays(2), summary.currentStartDate);
    }

    @Test
    public void missedYesterdayBreaksCurrentStreak() {
        LocalDate today = LocalDate.of(2026, 8, 23);
        repository.recordSensorDelta(today.minusDays(2), 5_000);

        assertEquals(0, repository.getStreakSummary(today).currentDays);
    }

    @Test
    public void downwardFitbitCorrectionRecomputesLongestStreak() {
        LocalDate first = LocalDate.of(2026, 8, 20);
        LocalDate second = first.plusDays(1);
        repository.initializeFitbitBaselines(List.of(fitbit(first, 0), fitbit(second, 0)));
        repository.applyFitbitSnapshots(List.of(fitbit(first, 5_100), fitbit(second, 5_100)));
        assertEquals(2, repository.getStreakSummary(second).longestDays);

        repository.applyFitbitSnapshots(List.of(fitbit(first, 4_900)));

        assertEquals(1, repository.getStreakSummary(second).longestDays);
        assertEquals(4_900, repository.getTotalSteps(first));
    }

    @Test
    public void reinitializingFitbitFreezesInactiveDifference() {
        LocalDate date = LocalDate.of(2026, 8, 23);
        repository.initializeFitbitBaselines(List.of(fitbit(date, 8_000)));
        repository.applyFitbitSnapshots(List.of(fitbit(date, 10_000)));

        repository.initializeFitbitBaselines(List.of(fitbit(date, 14_000)));
        repository.applyFitbitSnapshots(List.of(fitbit(date, 14_500)));

        assertEquals(2_500, repository.getTotalSteps(date));
    }

    @Test
    public void firstFitbitSyncAfterMigrationOnlyEstablishesBaselines() {
        LocalDate date = LocalDate.of(2026, 8, 23);
        StepEventRepository stepEvents = new StepEventRepository(database);
        DailyStepsRepository dailySteps = new DailyStepsRepository(database, stepEvents, repository);

        dailySteps.updateDailySteps(
                new FitbitSyncStateManager.FitbitSyncState(List.of(fitbit(date, 12_000))),
                false
        );

        assertEquals(0, repository.getTotalSteps(date));
        assertEquals(0, stepEvents.getUnhandledStepEvents(
                de.hd.stepwise.enums.StepSource.FITBIT
        ).size());
    }

    private FitbitSyncStateManager.DailyStepRecord fitbit(LocalDate date, int steps) {
        return new FitbitSyncStateManager.DailyStepRecord(
                date,
                steps,
                de.hd.stepwise.enums.StepSource.FITBIT
        );
    }
}
