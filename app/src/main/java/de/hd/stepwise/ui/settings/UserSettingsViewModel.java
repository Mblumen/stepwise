package de.hd.stepwise.ui.settings;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.fitbit.auth.FitbitAuthStateManager;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.pojos.events.Event;
import de.hd.stepwise.progresstracking.StepSourceManager;
import de.hd.stepwise.progresstracking.StepSyncScheduler;
import de.hd.stepwise.repositories.UserSettingsRepository;
import de.hd.stepwise.ui.BaseFragmentViewModel;

@HiltViewModel
public class UserSettingsViewModel extends BaseFragmentViewModel {

    private final UserSettingsRepository repository;
    private final FitbitAuthStateManager fitbitAuthStateManager;
    private final StepSyncScheduler stepSyncScheduler;
    private final MutableLiveData<Event<MethodResult>> _fitbitLoginResult = new MutableLiveData<>();
    private final StepSourceManager stepSourceManager;
    public LiveData<Event<MethodResult>> fitbitLoginResult = _fitbitLoginResult;

    @Inject
    public UserSettingsViewModel(@NonNull Application application, UserSettingsRepository userSettingsRepository, FitbitAuthStateManager fitbitAuthStateManager, StepSyncScheduler stepSyncScheduler, StepSourceManager stepSourceManager) {
        super(application, userSettingsRepository);
        this.repository = userSettingsRepository;
        this.fitbitAuthStateManager = fitbitAuthStateManager;
        this.stepSyncScheduler = stepSyncScheduler;
        this.stepSourceManager = stepSourceManager;
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

    public void updateRefreshTimeInMinutesFitbit(int refreshTime) {
        UserSettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.refreshTimeInMinutesFitbit = refreshTime;
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

    public boolean isAuthorized() {
        return fitbitAuthStateManager.isAuthorized();
    }

    public void processFitbitAuthResponse(AuthorizationResponse response, AuthorizationException ex) {

        if (response != null) {
            Log.d("UserSettingsViewModel", "Authorization successful");
            String code = response.authorizationCode;
            Log.d("UserSettingsViewModel", "Authorization code: " + code);

            fitbitAuthStateManager.exchangeAuthorizationCode(
                    response,
                    ex,
                    (accessToken, idToken, exception) -> {

                        if (exception != null) {
                            Log.e("Auth", "Token exchange failed", exception);
                            _fitbitLoginResult.postValue(new Event<>(new MethodResult(ResultStatus.ERROR, "Authentication Failed")));
                            return;
                        }

                        Log.d("Auth", "Access Token: " + accessToken);
                        _fitbitLoginResult.postValue(new Event<>(new MethodResult(ResultStatus.SUCCESS, "Authentication Successful")));

                    });

        } else if (ex != null) {
            Log.e("UserSettingsViewModel", "Authorization failed", ex);
        }
    }

    public void clearAuthorization() {
        fitbitAuthStateManager.clear();
         _fitbitLoginResult.postValue(new Event<>(new MethodResult(ResultStatus.SUCCESS, "Logged out from Fitbit")));
    }

    public void updateSelectedSensor(StepSource newSource) {
        UserSettings currentSettings = settings.getValue();

        if (currentSettings == null || currentSettings.stepSource == newSource) {
            return;
        }

        stepSourceManager.setStepSource(newSource, null);
    }

    public StepSource getCurrentStepSource() {
        UserSettings currentSettings = settings.getValue();
        return currentSettings != null ? currentSettings.stepSource : StepSource.STEP_COUNTER; // Default value
    }

}