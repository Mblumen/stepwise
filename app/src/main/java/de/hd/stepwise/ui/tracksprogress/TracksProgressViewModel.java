package de.hd.stepwise.ui.tracksprogress;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.pojos.UserProgressWithTrackAndMilestones;
import de.hd.stepwise.pojos.events.Event;
import de.hd.stepwise.pojos.ListItem;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.pojos.MilestoneWithStatus;
import de.hd.stepwise.pojos.events.FinishProgressResult;
import de.hd.stepwise.progresstracking.NotificationHandler;
import de.hd.stepwise.progresstracking.StepSyncScheduler;
import de.hd.stepwise.progresstracking.StepSyncWorker;
import de.hd.stepwise.repositories.MilestoneRepository;
import de.hd.stepwise.repositories.TrackRepository;
import de.hd.stepwise.repositories.UserProgressRepository;
import de.hd.stepwise.repositories.UserSettingsRepository;
import de.hd.stepwise.routing.RouteService;
import de.hd.stepwise.ui.tracks.BaseTracksViewModel;

@HiltViewModel
public class TracksProgressViewModel extends BaseTracksViewModel {

    private final LiveData<List<ListItem>> allProgress;
    private final UserProgressRepository userProgressRepository;
    private final RouteService routeService;
    private final NotificationHandler notifcationHandler;
    private final StepSyncScheduler stepSyncScheduler;

    private Track track;
    private float distanceWalked;
    private int stepsWalked;
    private long progressId;
    private final MutableLiveData<Event<MethodResult>> _methodResult = new MutableLiveData<>();
    public LiveData<Event<MethodResult>> observedResult = _methodResult;
    private final MutableLiveData<Event<List<GeoPoint>>> _geoData = new MutableLiveData<>();
    public LiveData<Event<List<GeoPoint>>> geoData = _geoData;
    private final MutableLiveData<Event<GeoPoint>> _pos = new MutableLiveData<>();
    public LiveData<Event<GeoPoint>> pos = _pos;
    private final MediatorLiveData<Boolean> _refreshing = new MediatorLiveData<>();
    public LiveData<Boolean> refreshing = _refreshing;
    private LiveData<WorkInfo> manualSyncWork;


    @Inject
    public TracksProgressViewModel(@NonNull Application application,
                                   UserProgressRepository userProgressRepository,
                                   MilestoneRepository milestoneRepository,
                                   UserSettingsRepository userSettingsRepository,
                                   TrackRepository trackRepository,
                                   RouteService routeService, NotificationHandler notifcationHandler,
                                   StepSyncScheduler stepSyncScheduler) {
        super(application, userSettingsRepository, trackRepository, milestoneRepository);
        this.userProgressRepository = userProgressRepository;
        this.routeService = routeService;
        this.notifcationHandler = notifcationHandler;
        this.stepSyncScheduler = stepSyncScheduler;
        allProgress = userProgressRepository.getProgressWithMilestonesForStatusWithSeparators(
                userSettingsRepository.getShowCompletedTracks()
        );
    }

    public void refreshSteps() {
        if (Boolean.TRUE.equals(_refreshing.getValue())) return;
        _refreshing.setValue(true);
        manualSyncWork = stepSyncScheduler.triggerManualSync();
        _refreshing.addSource(manualSyncWork, workInfo -> {
            if (workInfo == null || !workInfo.getState().isFinished()) return;
            _refreshing.removeSource(manualSyncWork);
            manualSyncWork = null;
            _refreshing.setValue(false);
            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                _methodResult.setValue(new Event<>(new MethodResult(
                        ResultStatus.SUCCESS, "Step data refreshed")));
            } else {
                String error = workInfo.getOutputData().getString(StepSyncWorker.OUTPUT_ERROR);
                _methodResult.setValue(new Event<>(new MethodResult(ResultStatus.ERROR,
                        error == null ? "Could not refresh step data" : error)));
            }
        });
    }

    public void setTrack(Track track) {
        this.track = track;
    }
    public void setDistanceWalked(float distanceWalked) {
        this.distanceWalked = distanceWalked;
    }

    public void setStepsWalked(int stepsWalked) {
        this.stepsWalked = stepsWalked;
    }

    public void setProgressId(long progressId) {
        this.progressId = progressId;
    }
    public LiveData<List<MilestoneWithStatus>> getAllMilestones() { return milestoneRepository.getMilestonesWithStatus(track.id, progressId, distanceWalked, stepsWalked); }
    public LiveData<List<ListItem>> getAllProgress() {
        return allProgress;
    }

    public void pauseTrackProgress(long progressId) {
        MutableLiveData<MethodResult> result = new MutableLiveData<>();
        userProgressRepository.pauseTrackProgress(progressId).observeForever(new Observer<>() {
            @Override
            public void onChanged(MethodResult methodResult) {
                if (methodResult.status == ResultStatus.SUCCESS) {
                    _methodResult.postValue(new Event<>(methodResult));
                } else {
                    Log.e("TracksProgressViewModel", "Error pausing track progress: " + methodResult.message);
                }
                result.removeObserver(this); // very important to prevent leaks
            }
        });
    }

    public void resumeTrackProgress(long progressId) {
        MutableLiveData<MethodResult> result = new MutableLiveData<>();
        userProgressRepository.resumeTrackProgress(progressId).observeForever(new Observer<>() {
            @Override
            public void onChanged(MethodResult methodResult) {
                if (methodResult.status == ResultStatus.SUCCESS) {
                    _methodResult.postValue(new Event<>(methodResult));
                } else {
                    Log.e("TracksProgressViewModel", "Error resuming track progress: " + methodResult.message);
                }
                result.removeObserver(this); // very important to prevent leaks
            }
        });
    }
    public void finishTrack(long progressId) {
        LiveData<FinishProgressResult> source = userProgressRepository.finishProgress(progressId);
        Observer<FinishProgressResult> observer = new Observer<>() {
            @Override
            public void onChanged(FinishProgressResult finishProgressResult) {
                if (finishProgressResult.methodResult.status == ResultStatus.SUCCESS) {
                    _methodResult.postValue(new Event<>(finishProgressResult.methodResult));
                    notifcationHandler.handleStepUpdate(finishProgressResult.stepUpdateResult);
                } else {
                    Log.e("TracksProgressViewModel", "Error finishing progress: " + finishProgressResult.methodResult.message);
                }
                source.removeObserver(this);
            }
        };
        source.observeForever(observer);
    }

    public void calculateAndPostPosition(UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<GeoPoint> geoData = routeService.getGeoData(userProgressWithTrackAndMilestones);
                _geoData.postValue(new Event<>(geoData));
                GeoPoint positionForDistanceWalked = routeService.getPosition(geoData, userProgressWithTrackAndMilestones.userProgress.distanceWalked);
                _pos.postValue(new Event<>(positionForDistanceWalked));
            } catch (JSONException | IOException e) {
                Log.e("TracksProgressViewModel", "Error calculating position: " + e.getMessage());
            }
        });

    }
}
