package de.hd.fitbittracks.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import de.hd.fitbittracks.entities.UserSettings;
import de.hd.fitbittracks.repositories.UserSettingsRepository;

public class UserSettingsViewModel extends AndroidViewModel {
    private final UserSettingsRepository repository;
    public LiveData<UserSettings> settings;

    public UserSettingsViewModel(@NonNull Application application) {
        super(application);
        repository = new UserSettingsRepository(application);
        settings = repository.getSettings();
    }

    public void saveSettings(UserSettings updatedSettings) {
        repository.updateSettings(updatedSettings);
    }

    public void updateStepLength(float stepLength) {
        UserSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.stepLengthInMeters = stepLength;
            saveSettings(currentSettings);
        }
    }

    public void updateUseDarkMode(boolean useDarkMode) {
        UserSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.useDarkMode = useDarkMode;
            saveSettings(currentSettings);
        }
    }

    public void updateShowCompletedTracks(boolean showCompletedTracks) {
        UserSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.showCompletedTracks = showCompletedTracks;
            saveSettings(currentSettings);
        }
    }

}