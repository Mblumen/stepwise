package de.hd.fitbittracks.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import de.hd.fitbittracks.entities.UserSettings;
import de.hd.fitbittracks.repositories.UserSettingsRepository;

public abstract class BaseViewModel extends AndroidViewModel {
    private final LiveData<UserSettings> settings;
    public BaseViewModel(@NonNull Application application) {
        super(application);
        UserSettingsRepository userSettingsRepository = new UserSettingsRepository(application);
        settings = userSettingsRepository.getSettings();
    }

    public LiveData<UserSettings> getSettings() {
        return settings;
    }
}
