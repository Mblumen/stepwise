package de.hd.stepwise.repositories;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.DailyStepsDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.DailySteps;
import de.hd.stepwise.entities.StepEvent;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;

@Singleton
public class DailyStepsRepository {
    private final DailyStepsDao dailyStepsDao;
    private final StepEventRepository stepEventRepository;
    @Inject
    public DailyStepsRepository(AppDatabase db, StepEventRepository stepEventRepository) {
        dailyStepsDao = db.dailyStepsDao();
        this.stepEventRepository = stepEventRepository;
    }

    public void updateDailySteps(FitbitSyncStateManager.FitbitSyncState syncState, boolean initSteps) {
        List<DailySteps> dailyStepsList = new ArrayList<>();
        syncState.records.forEach(record -> {
            DailySteps dailySteps = dailyStepsDao.getByDateAndSource(record.date.toString(), record.source);
            if(dailySteps == null) dailySteps = new DailySteps();
            dailySteps.date = record.date.toString();
            int stepDifference =  initSteps ? 0 : record.steps - dailySteps.steps;
            //dailySteps.addedStepsSinceLastUpdate = initSteps ? 0 : record.steps - dailySteps.steps;
            dailySteps.steps = record.steps;
            dailySteps.source = record.source;
            dailyStepsList.add(dailySteps);
            if(stepDifference  > 0) stepEventRepository.addStepEvent(new StepEvent(stepDifference, record.source, (new Date()).getTime()));
        });
        dailyStepsDao.insertOrUpdateList(dailyStepsList);
    }

    public LiveData<List<DailySteps>> getDailyStepsWithUpdateBySource(StepSource source) {
        return dailyStepsDao.observeWithUpdateBySource(source);
    }

    public void updateValueRead(String date) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DailySteps dailySteps = dailyStepsDao.getByDate(date);
            if(dailySteps != null) {
                dailySteps.addedStepsSinceLastUpdate = 0;
                dailyStepsDao.insertOrUpdate(dailySteps);
            }
        });
    }

    public void resetAddedSteps(StepSource stepSource) {
        dailyStepsDao.resetAddedStepsSinceLastUpdate(stepSource);
    }
}
