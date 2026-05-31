package de.hd.stepwise.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.UserSettingsDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.pojos.events.Event;

@Singleton
public class UserSettingsRepository {
    private final UserSettingsDao userSettingsDao;
    private final MutableLiveData<Event<StepSource>> stepSourceChangeEvents = new MutableLiveData<>();

    @Inject
    public UserSettingsRepository(AppDatabase db) {
        userSettingsDao = db.userSettingsDao();
    }

    public LiveData<UserSettings> getSettings() {
        return userSettingsDao.getSettingsLive();
    }

    public LiveData<Event<StepSource>> getStepSourceChangeEvents() {
        return stepSourceChangeEvents;
    }
    public void updateSettings(UserSettings settings) {
        Executors.newSingleThreadExecutor().execute(() -> {
            userSettingsDao.insertOrUpdate(settings);
        });
    }

    public void updateStepSource(StepSource stepSource) {
        UserSettings currentSettings = userSettingsDao.getSettings();
        if (currentSettings != null) {
            currentSettings.stepSource = stepSource;
            userSettingsDao.insertOrUpdate(currentSettings);
            stepSourceChangeEvents.postValue(new Event<>(stepSource));
        }
    }

    public LiveData<Boolean> getShowCompletedTracks() {
        return userSettingsDao.getShowCompletedTracks(); // e.g., LiveData from Room
    }

    public LiveData<Boolean> getShowLockedMilestones() {
        return userSettingsDao.getShowLockedMilestones(); // e.g., LiveData from Room
    }

    public LiveData<Float> getStepLength() {
        return userSettingsDao.getStepLength(); // e.g., LiveData from Room
    }

    public LiveData<StepSource> getStepSource() {
        MutableLiveData<StepSource> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            StepSource stepSource = userSettingsDao.getStepSource();
            result.postValue(stepSource);
        });
        return result;
    }

    public StepSource getStepSourceSync() {
        return userSettingsDao.getStepSource();
    }

    public LiveData<Integer> getRefreshTimeInMinutesFitbitLive() {
        return userSettingsDao.getRefreshTimeInMinutesFitbitLive();
    }

    public Integer getRefreshTimeInMinutesFitbit() {
        return userSettingsDao.getRefreshTimeInMinutesFitbit();
    }
}