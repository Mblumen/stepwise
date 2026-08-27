package de.hd.stepwise.helper.fitbit;

import android.util.Log;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.googlehealth.GoogleHealthApiService;
import de.hd.stepwise.repositories.DailyStepsRepository;

@Singleton
public class FitbitSyncStateManager {

    public static class DailyStepRecord {
        public DailyStepRecord(LocalDate date, int steps, StepSource source) {
            this.date = date;
            this.steps = steps;
            this.source = source;
        }
        public LocalDate date;
        public int steps;
        public StepSource source;
    }
    public static class FitbitSyncState{

        public List<DailyStepRecord> records;
        public FitbitSyncState(List<DailyStepRecord> records) {
            this.records = records;
        }
    }

    private final GoogleHealthApiService googleHealthApiService;
    private final DailyStepsRepository dailyStepsRepository;

    @Inject
    public FitbitSyncStateManager(GoogleHealthApiService googleHealthApiService, DailyStepsRepository dailyStepsRepository) {
        this.googleHealthApiService = googleHealthApiService;
        this.dailyStepsRepository = dailyStepsRepository;
    }

    public void save(FitbitSyncState fitbitSyncState, boolean initSteps) {
        dailyStepsRepository.updateDailySteps(fitbitSyncState, initSteps);
    }

    public void startStepTracking(Consumer<Boolean> callback) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        getStepsForStartAndEndDate(weekAgo, today, apiResponse -> {
            if (!isSuccessfulResponse(apiResponse)) {
                Log.d("GoogleHealthSync", "Could not initialize Google Health step tracking");
                if (callback != null) callback.accept(false);
                return;
            }
            save(new FitbitSyncState(apiResponse), true);
            if (callback != null) callback.accept(true);
        });
    }

    public void stopStepTracking(Runnable callback) {
        dailyStepsRepository.resetAddedSteps(StepSource.FITBIT);
        if(callback != null) callback.run();
    }

    public void getStepDataPastWeek(Consumer<List<DailyStepRecord>> callback) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        Log.d("FitbitSync", "7 days ago: " + weekAgo + ", today: " + today);
        getStepsForStartAndEndDate(weekAgo, today, callback);
    }

    public List<DailyStepRecord> getStepDataPastWeekSync() throws Exception {
        LocalDate today = LocalDate.now();
        return googleHealthApiService.getStepsData(today.minusDays(7), today);
    }

    /*public void getStepsSinceLastUpdate(Consumer<List<DailyStepRecord>> callback) {
        Log.d("FitbitSync", "Starting Fitbit sync process");
        FitbitSyncState fitbitSyncState = read();
        LocalDate lastSyncDate = fitbitSyncState.date;
        int lastSteps = fitbitSyncState.steps;
        LocalDate today = LocalDate.now();
        Log.d("FitbitSync", "Last sync date: " + lastSyncDate + ", last known steps: " + lastSteps);
        this.getStepsForStartAndEndDate(lastSyncDate, today, (List<DailyStepRecord> apiResponse) -> {
            if(apiResponse.isEmpty()) {
                Log.d("FitbitSync", "No data received from Fitbit API");
                if(callback != null) callback.accept(apiResponse);
                return;
            }

            DailyStepRecord firstRecord = apiResponse.get(0);
            DailyStepRecord lastRecord = apiResponse.get(apiResponse.size() - 1);
            LocalDate lastDate = lastRecord.date;
            int lastDailySteps = lastRecord.steps;

            Log.d("FitbitSync", "First record date: " + firstRecord.date + ", steps: " + firstRecord.steps + "; Last record date: " + lastRecord.date + ", steps: " + lastDailySteps);
            if(firstRecord.date.equals(lastSyncDate) && lastSteps > 0) {
                firstRecord.steps -= lastSteps;
            }

            updateSyncState(lastDate, lastDailySteps);
            if(callback != null) callback.accept(apiResponse);
        });
    }*/

    private void getStepsForStartAndEndDate(LocalDate startDate, LocalDate endDate, Consumer<List<DailyStepRecord>> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                callback.accept(googleHealthApiService.getStepsData(startDate, endDate));
            } catch (Exception exception) {
                Log.e("GoogleHealthSync", "Could not read Google Health steps", exception);
                callback.accept(null);
            }
        });
    }

    static boolean isSuccessfulResponse(List<DailyStepRecord> records) {
        // An empty rollup is a valid response when the account has no step history.
        // Only null represents a request failure in this callback chain.
        return records != null;
    }
}
