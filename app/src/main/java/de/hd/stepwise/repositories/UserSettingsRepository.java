package de.hd.stepwise.repositories;

import androidx.lifecycle.LiveData;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.UserSettingsDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.UserSettings;

@Singleton
public class UserSettingsRepository {
    private final UserSettingsDao userSettingsDao;

    @Inject
    public UserSettingsRepository(AppDatabase db) {
        userSettingsDao = db.userSettingsDao();
    }

    public LiveData<UserSettings> getSettings() {
        return userSettingsDao.getSettingsLive();
    }

    public void updateSettings(UserSettings settings) {
        Executors.newSingleThreadExecutor().execute(() -> {
            userSettingsDao.insertOrUpdate(settings);
        });
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
}