package de.hd.stepwise.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.MediatorLiveData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.DailyActivityDao;
import de.hd.stepwise.daos.StreakLedgerDao;
import de.hd.stepwise.daos.UserSettingsDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.DailyActivity;
import de.hd.stepwise.entities.DailyStepGoal;
import de.hd.stepwise.entities.DailyStreakOutcome;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;
import de.hd.stepwise.pojos.StreakSummary;
import de.hd.stepwise.pojos.TodayStepStatus;
import de.hd.stepwise.pojos.DailyGoalState;

@Singleton
public class DailyActivityRepository {
    public static final int ACTIVE_DAY_STEPS = 5_000;

    private final AppDatabase database;
    private final DailyActivityDao dailyActivityDao;
    private final StreakLedgerDao streakLedgerDao;
    private final UserSettingsDao userSettingsDao;

    @Inject
    public DailyActivityRepository(AppDatabase database) {
        this.database = database;
        this.dailyActivityDao = database.dailyActivityDao();
        this.streakLedgerDao = database.streakLedgerDao();
        this.userSettingsDao = database.userSettingsDao();
    }

    public void recordSensorDelta(LocalDate date, int steps) {
        if (steps <= 0) return;
        database.runInTransaction(() -> {
            DailyActivity activity = getOrCreate(date);
            activity.sensorSteps += steps;
            dailyActivityDao.insertOrUpdate(activity);
        });
        reconcile(LocalDate.now());
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
        int positiveDelta = database.runInTransaction(() -> {
            int accumulatedDelta = 0;
            boolean trackingWasInitialized = dailyActivityDao.countFitbitBaselines() > 0;
            for (FitbitSyncStateManager.DailyStepRecord record : records) {
                DailyActivity activity = getOrCreate(record.date);
                if (activity.fitbitLastObserved == null) {
                    activity.fitbitLastObserved = record.steps;
                    if (trackingWasInitialized) {
                        activity.fitbitSteps += record.steps;
                        accumulatedDelta += record.steps;
                    }
                } else {
                    int difference = record.steps - activity.fitbitLastObserved;
                    int corrected = Math.max(0, activity.fitbitSteps + difference);
                    accumulatedDelta += Math.max(0, corrected - activity.fitbitSteps);
                    activity.fitbitSteps = corrected;
                    activity.fitbitLastObserved = record.steps;
                }
                dailyActivityDao.insertOrUpdate(activity);
            }
            return accumulatedDelta;
        });
        reconcile(LocalDate.now());
        return positiveDelta;
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
        StreakLedger.Result result = calculate(today, dailyActivityDao.getAll(),
                streakLedgerDao.getGoals(), userSettingsDao.getSettings());
        persistOutcomes(result.outcomes);
        return result.summary;
    }

    public LiveData<StreakSummary> observeStreakSummary() {
        return observeCalculation(LocalDate.now(), CalculationValue.STREAK);
    }

    public LiveData<TodayStepStatus> observeTodayStatus(LocalDate today) {
        return observeCalculation(today, CalculationValue.TODAY);
    }

    public LiveData<DailyGoalState> observeDailyGoalState(LocalDate today) {
        return Transformations.map(streakLedgerDao.observeGoals(), goals -> goalState(goals, today));
    }

    public DailyGoalState getDailyGoalState(LocalDate today) {
        return goalState(streakLedgerDao.getGoals(), today);
    }

    public void scheduleDailyGoal(int steps, LocalDate today) {
        if (!isValidGoal(steps)) throw new IllegalArgumentException("Invalid daily step goal");
        database.runInTransaction(() -> {
            List<DailyStepGoal> goals = streakLedgerDao.getGoals();
            int activeGoal = goalFor(goals, today);
            streakLedgerDao.deleteGoalsAfter(today.toString());
            if (steps != activeGoal) {
                streakLedgerDao.upsertGoal(new DailyStepGoal(today.plusDays(1).toString(), steps));
            }
        });
    }

    public static boolean isValidGoal(int steps) {
        return steps >= 1_000 && steps <= 50_000 && steps % 500 == 0;
    }

    public void refreshStreakLedger(LocalDate today) {
        Executors.newSingleThreadExecutor().execute(() -> reconcile(today));
    }

    private DailyActivity getOrCreate(LocalDate date) {
        DailyActivity activity = dailyActivityDao.getByDate(date.toString());
        return activity == null ? new DailyActivity(date.toString()) : activity;
    }

    @SuppressWarnings("unchecked")
    private <T> LiveData<T> observeCalculation(LocalDate today, CalculationValue value) {
        MediatorLiveData<T> result = new MediatorLiveData<>();
        final List<DailyActivity>[] activities = new List[]{null};
        final List<DailyStepGoal>[] goals = new List[]{null};
        final UserSettings[] settings = new UserSettings[]{null};
        Runnable publish = () -> {
            if (activities[0] == null || goals[0] == null) return;
            StreakLedger.Result calculation = calculate(today, activities[0], goals[0], settings[0]);
            result.setValue((T) (value == CalculationValue.STREAK
                    ? calculation.summary : calculation.todayStatus));
        };
        result.addSource(dailyActivityDao.observeAll(), rows -> {
            activities[0] = rows == null ? List.of() : rows;
            publish.run();
        });
        result.addSource(streakLedgerDao.observeGoals(), rows -> {
            goals[0] = rows == null ? List.of() : rows;
            publish.run();
        });
        result.addSource(userSettingsDao.getSettingsLive(), row -> {
            settings[0] = row;
            publish.run();
        });
        return result;
    }

    private StreakLedger.Result calculate(LocalDate today, List<DailyActivity> activities,
                                          List<DailyStepGoal> goals, UserSettings settings) {
        Map<LocalDate, Integer> stepsByDate = new HashMap<>();
        LocalDate earliestActivity = today;
        for (DailyActivity activity : activities) {
            LocalDate date = LocalDate.parse(activity.date);
            stepsByDate.put(date, activity.getTotalSteps());
            if (date.isBefore(earliestActivity)) earliestActivity = date;
        }
        LocalDate activationDate = settings == null || settings.streakTrackingStartDate.isBlank()
                ? earliestActivity : LocalDate.parse(settings.streakTrackingStartDate);
        NavigableMap<LocalDate, Integer> goalsByDate = goalMap(goals, activationDate);
        return StreakLedger.calculate(stepsByDate, goalsByDate, activationDate, today);
    }

    private NavigableMap<LocalDate, Integer> goalMap(List<DailyStepGoal> goals,
                                                      LocalDate activationDate) {
        NavigableMap<LocalDate, Integer> result = new TreeMap<>();
        for (DailyStepGoal goal : goals) {
            result.put(LocalDate.parse(goal.effectiveDate), goal.steps);
        }
        if (result.isEmpty()) result.put(activationDate, ACTIVE_DAY_STEPS);
        return result;
    }

    private DailyGoalState goalState(List<DailyStepGoal> goals, LocalDate today) {
        int active = goalFor(goals, today);
        for (DailyStepGoal goal : goals) {
            LocalDate effectiveDate = LocalDate.parse(goal.effectiveDate);
            if (effectiveDate.isAfter(today)) {
                return new DailyGoalState(active, goal.steps, effectiveDate);
            }
        }
        return new DailyGoalState(active, null, null);
    }

    private int goalFor(List<DailyStepGoal> goals, LocalDate date) {
        int active = ACTIVE_DAY_STEPS;
        LocalDate activeDate = LocalDate.MIN;
        for (DailyStepGoal goal : goals) {
            LocalDate effectiveDate = LocalDate.parse(goal.effectiveDate);
            if (!effectiveDate.isAfter(date) && effectiveDate.isAfter(activeDate)) {
                active = goal.steps;
                activeDate = effectiveDate;
            }
        }
        return active;
    }

    private void persistOutcomes(List<StreakLedger.Outcome> outcomes) {
        List<DailyStreakOutcome> rows = new ArrayList<>();
        for (StreakLedger.Outcome outcome : outcomes) {
            rows.add(new DailyStreakOutcome(outcome.date.toString(), outcome.goalSteps,
                    outcome.qualified, outcome.reserveUsed, outcome.reserveAfter));
        }
        database.runInTransaction(() -> {
            streakLedgerDao.deleteOutcomes();
            if (!rows.isEmpty()) streakLedgerDao.insertOutcomes(rows);
        });
    }

    private void reconcile(LocalDate today) {
        StreakLedger.Result result = calculate(today, dailyActivityDao.getAll(),
                streakLedgerDao.getGoals(), userSettingsDao.getSettings());
        persistOutcomes(result.outcomes);
    }

    private enum CalculationValue { STREAK, TODAY }

}
