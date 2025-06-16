package de.hd.fitbittracks.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.concurrent.Executors;

import de.hd.fitbittracks.daos.UserSettingsDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.UserSettings;

public class UserSettingsRepository {
    private final UserSettingsDao userSettingsDao;

    public UserSettingsRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
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
}