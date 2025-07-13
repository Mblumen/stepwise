package de.hd.fitbittracks.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.fitbittracks.entities.UserSettings;
import de.hd.fitbittracks.repositories.UserSettingsRepository;

public abstract class BaseViewModel extends AndroidViewModel {
    protected final LiveData<UserSettings> settings;
    protected final LiveData<Float> stepLength;

    public BaseViewModel(@NonNull Application application, UserSettingsRepository userSettingsRepository) {
        super(application);
        settings = userSettingsRepository.getSettings();
        stepLength = userSettingsRepository.getStepLength();
    }

    public LiveData<UserSettings> getSettings() {
        return settings;
    }

    public LiveData<Float> getStepLength() {
        return stepLength;
    }
}
