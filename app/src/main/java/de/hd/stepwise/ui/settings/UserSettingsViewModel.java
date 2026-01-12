package de.hd.stepwise.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.repositories.UserSettingsRepository;
import de.hd.stepwise.ui.BaseFragmentViewModel;

@HiltViewModel
public class UserSettingsViewModel extends BaseFragmentViewModel {

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

    public void updateShowLockedMilestones(boolean showLockedMilestones) {
        UserSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.showLockedMilestones = showLockedMilestones;
            saveSettings(currentSettings);
        }
    }

}