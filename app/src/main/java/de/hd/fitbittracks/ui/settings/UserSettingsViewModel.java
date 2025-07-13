package de.hd.fitbittracks.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.fitbittracks.entities.UserSettings;
import de.hd.fitbittracks.repositories.UserSettingsRepository;
import de.hd.fitbittracks.ui.BaseViewModel;

@HiltViewModel
public class UserSettingsViewModel extends BaseViewModel {

    private final UserSettingsRepository repository;
    @Inject
    public UserSettingsViewModel(@NonNull Application application, UserSettingsRepository userSettingsRepository) {
        super(application, userSettingsRepository);
        this.repository = userSettingsRepository;
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