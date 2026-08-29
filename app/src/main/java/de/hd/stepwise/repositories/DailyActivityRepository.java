package de.hd.stepwise.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.DailyActivityDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.DailyActivity;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;
import de.hd.stepwise.pojos.StreakSummary;

@Singleton
public class DailyActivityRepository {
    public static final int ACTIVE_DAY_STEPS = 5_000;

    private final AppDatabase database;
    private final DailyActivityDao dailyActivityDao;

    @Inject
    public DailyActivityRepository(AppDatabase database) {
        this.database = database;
        this.dailyActivityDao = database.dailyActivityDao();
    }

    public void recordSensorDelta(LocalDate date, int steps) {
        if (steps <= 0) return;
        database.runInTransaction(() -> {
            DailyActivity activity = getOrCreate(date);
            activity.sensorSteps += steps;
            dailyActivityDao.insertOrUpdate(activity);
        });
    }

    public void initializeFitbitBaselines(List<FitbitSyncStateManager.DailyStepRecord> records) {
        database.runInTransaction(() -> {
            for (FitbitSyncStateManager.DailyStepRecord record : records) {
                DailyActivity activity = getOrCreate(record.date);
                activity.fitbitLastObserved = record.steps;
                dailyActivityDao.insertOrUpdate(activity);
            }
        });
    }

    public int applyFitbitSnapshots(List<FitbitSyncStateManager.DailyStepRecord> records) {
        return database.runInTransaction(() -> {
            int positiveDelta = 0;
            boolean trackingWasInitialized = dailyActivityDao.countFitbitBaselines() > 0;
            for (FitbitSyncStateManager.DailyStepRecord record : records) {
                DailyActivity activity = getOrCreate(record.date);
                if (activity.fitbitLastObserved == null) {
                    activity.fitbitLastObserved = record.steps;
                    if (trackingWasInitialized) {
                        activity.fitbitSteps += record.steps;
                        positiveDelta += record.steps;
                    }
                } else {
                    int difference = record.steps - activity.fitbitLastObserved;
                    int corrected = Math.max(0, activity.fitbitSteps + difference);
                    positiveDelta += Math.max(0, corrected - activity.fitbitSteps);
                    activity.fitbitSteps = corrected;
                    activity.fitbitLastObserved = record.steps;
                }
                dailyActivityDao.insertOrUpdate(activity);
            }
            return positiveDelta;
        });
    }

    public int getTotalSteps(LocalDate date) {
        DailyActivity activity = dailyActivityDao.getByDate(date.toString());
        return activity == null ? 0 : activity.getTotalSteps();
    }

    public LiveData<Integer> observeTotalSteps(LocalDate date) {
        return Transformations.map(dailyActivityDao.observeByDate(date.toString()),
                activity -> activity == null ? 0 : activity.getTotalSteps());
    }

    public boolean hasFitbitBaseline() {
        return dailyActivityDao.countFitbitBaselines() > 0;
    }

    public StreakSummary getStreakSummary(LocalDate today) {
        return calculateStreakSummary(dailyActivityDao.getAll(), LocalDate.MIN, today);
    }

    public LiveData<StreakSummary> observeStreakSummary() {
        return Transformations.map(dailyActivityDao.observeAll(),
                activities -> calculateStreakSummary(activities, LocalDate.MIN, LocalDate.now()));
    }

    private DailyActivity getOrCreate(LocalDate date) {
        DailyActivity activity = dailyActivityDao.getByDate(date.toString());
        return activity == null ? new DailyActivity(date.toString()) : activity;
    }

    static StreakSummary calculateStreakSummary(List<DailyActivity> activities,
                                                 LocalDate trackingStartDate,
                                                 LocalDate today) {
        List<LocalDate> qualifyingDates = new ArrayList<>();
        for (DailyActivity activity : activities) {
            LocalDate date = LocalDate.parse(activity.date);
            if (!date.isBefore(trackingStartDate) && !date.isAfter(today)
                    && activity.getTotalSteps() >= ACTIVE_DAY_STEPS) {
                qualifyingDates.add(date);
            }
        }

        int longestDays = 0;
        LocalDate longestStart = null;
        LocalDate longestEnd = null;
        int runDays = 0;
        LocalDate runStart = null;
        LocalDate previous = null;
        for (LocalDate date : qualifyingDates) {
            if (previous == null || ChronoUnit.DAYS.between(previous, date) != 1) {
                runDays = 1;
                runStart = date;
            } else {
                runDays++;
            }
            if (runDays > longestDays) {
                longestDays = runDays;
                longestStart = runStart;
                longestEnd = date;
            }
            previous = date;
        }

        LocalDate currentEnd = qualifyingDates.isEmpty()
                ? null : qualifyingDates.get(qualifyingDates.size() - 1);
        int currentDays = 0;
        LocalDate currentStart = null;
        if (currentEnd != null
                && (currentEnd.equals(today) || currentEnd.equals(today.minusDays(1)))) {
            currentDays = 1;
            currentStart = currentEnd;
            for (int i = qualifyingDates.size() - 2; i >= 0; i--) {
                LocalDate date = qualifyingDates.get(i);
                if (ChronoUnit.DAYS.between(date, currentStart) != 1) break;
                currentStart = date;
                currentDays++;
            }
        }
        return new StreakSummary(currentDays, longestDays, currentStart, longestStart, longestEnd);
    }
}
