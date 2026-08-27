package de.hd.stepwise.ui.settings;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;
import java.util.function.Consumer;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;
import de.hd.stepwise.helper.googlehealth.GoogleHealthAuthManager;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.pojos.events.Event;
import de.hd.stepwise.progresstracking.StepSourceManager;
import de.hd.stepwise.progresstracking.StepSyncScheduler;
import de.hd.stepwise.repositories.UserSettingsRepository;
import de.hd.stepwise.ui.BaseFragmentViewModel;

@HiltViewModel
public class UserSettingsViewModel extends BaseFragmentViewModel {

    interface AuthorizationRevoker {
        void revoke(String accountName, Runnable successCallback,
                    Consumer<Exception> errorCallback);
    }

    interface SourceSwitcher {
        void setStepSource(StepSource source, Consumer<MethodResult> callback);
    }

    private final UserSettingsRepository repository;
    private final GoogleHealthAuthManager googleHealthAuthManager;
    private final StepSyncScheduler stepSyncScheduler;
    private final MutableLiveData<Event<MethodResult>> _fitbitLoginResult = new MutableLiveData<>();
    private final MutableLiveData<Event<PendingIntent>> _googleHealthAuthorizationResolution =
            new MutableLiveData<>();
    private final StepSourceManager stepSourceManager;
    private final FitbitSyncStateManager fitbitSyncStateManager;
    public LiveData<Event<MethodResult>> fitbitLoginResult = _fitbitLoginResult;
    public LiveData<Event<PendingIntent>> googleHealthAuthorizationResolution =
            _googleHealthAuthorizationResolution;

    @Inject
    public UserSettingsViewModel(@NonNull Application application, UserSettingsRepository userSettingsRepository, GoogleHealthAuthManager googleHealthAuthManager, StepSyncScheduler stepSyncScheduler, StepSourceManager stepSourceManager, FitbitSyncStateManager fitbitSyncStateManager) {
        super(application, userSettingsRepository);
        this.repository = userSettingsRepository;
        this.googleHealthAuthManager = googleHealthAuthManager;
        this.stepSyncScheduler = stepSyncScheduler;
        this.stepSourceManager = stepSourceManager;
        this.fitbitSyncStateManager = fitbitSyncStateManager;
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
        return googleHealthAuthManager.isAuthorized();
    }

    public String getConnectedGoogleAccountName() {
        return googleHealthAuthManager.getConnectedAccountName();
    }

    public void connectGoogleHealth(String accountName) {
        googleHealthAuthManager.authorize(accountName,
                pendingIntent -> _googleHealthAuthorizationResolution.postValue(
                        new Event<>(pendingIntent)),
                this::finishGoogleHealthConnection,
                this::reportAuthenticationFailure);
    }

    public void processGoogleHealthAuthorizationResult(Intent data) {
        googleHealthAuthManager.completeAuthorization(
                data,
                this::finishGoogleHealthConnection,
                this::reportAuthenticationFailure);
    }

    public void clearAuthorization(String accountName) {
        disconnectGoogleHealth(accountName, googleHealthAuthManager::revoke,
                stepSourceManager::setStepSource,
                result -> _fitbitLoginResult.postValue(new Event<>(result)),
                exception -> {
                    Log.e("GoogleHealthAuth", "Could not revoke Google Health access", exception);
                    _fitbitLoginResult.postValue(new Event<>(new MethodResult(
                            ResultStatus.ERROR, "Could not disconnect Google Health")));
                });
    }

    private void reportAuthenticationSuccess() {
        _fitbitLoginResult.postValue(new Event<>(new MethodResult(
                ResultStatus.SUCCESS, "Google Health connected")));
    }

    private void finishGoogleHealthConnection() {
        if (getCurrentStepSource() != StepSource.FITBIT) {
            reportAuthenticationSuccess();
            return;
        }
        // An upgraded user can still have legacy Fitbit baselines. Establish Google
        // Health baselines before the worker applies deltas from the new data source.
        fitbitSyncStateManager.startStepTracking(success -> {
            if (success) {
                reportAuthenticationSuccess();
            } else {
                _fitbitLoginResult.postValue(new Event<>(new MethodResult(
                        ResultStatus.ERROR, "Connected, but could not initialize Google Health steps")));
            }
        });
    }

    private void reportAuthenticationFailure(Exception exception) {
        Log.e("GoogleHealthAuth", "Google Health authorization failed", exception);
        _fitbitLoginResult.postValue(new Event<>(new MethodResult(
                ResultStatus.ERROR, "Google Health authentication failed")));
    }

    static void disconnectGoogleHealth(String accountName, AuthorizationRevoker revoker,
                                       SourceSwitcher sourceSwitcher,
                                       Consumer<MethodResult> resultCallback,
                                       Consumer<Exception> revokeErrorCallback) {
        revoker.revoke(accountName,
                () -> sourceSwitcher.setStepSource(StepSource.STEP_COUNTER, sourceResult -> {
                    if (sourceResult.status == ResultStatus.SUCCESS) {
                        resultCallback.accept(new MethodResult(
                                ResultStatus.SUCCESS, "Disconnected from Google Health"));
                    } else {
                        resultCallback.accept(new MethodResult(
                                ResultStatus.ERROR,
                                "Disconnected from Google Health, but the phone step counter is unavailable"));
                    }
                }),
                revokeErrorCallback);
    }

    public void updateSelectedSensor(StepSource newSource) {
        UserSettings currentSettings = settings.getValue();

        if (currentSettings == null || currentSettings.stepSource == newSource) {
            return;
        }

        stepSourceManager.setStepSource(newSource,
                result -> _fitbitLoginResult.postValue(new Event<>(result)));
    }

    public StepSource getCurrentStepSource() {
        UserSettings currentSettings = settings.getValue();
        return currentSettings != null ? currentSettings.stepSource : StepSource.STEP_COUNTER; // Default value
    }

}
