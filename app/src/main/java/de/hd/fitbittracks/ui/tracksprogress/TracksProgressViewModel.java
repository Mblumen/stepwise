package de.hd.fitbittracks.ui.tracksprogress;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.enums.ResultStatus;
import de.hd.fitbittracks.pojos.UserProgressWithTrack;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.pojos.events.Event;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;
import de.hd.fitbittracks.repositories.MilestoneRepository;
import de.hd.fitbittracks.repositories.UserProgressRepository;
import de.hd.fitbittracks.repositories.UserSettingsRepository;
import de.hd.fitbittracks.routing.RouteFetcher;
import de.hd.fitbittracks.routing.RouteService;
import de.hd.fitbittracks.ui.BaseViewModel;

@HiltViewModel
public class TracksProgressViewModel extends BaseViewModel {

    private final LiveData<List<ListItem>> allProgress;
    private final MilestoneRepository milestoneRepository;
    private final UserProgressRepository userProgressRepository;
    private final RouteService routeService;
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


    @Inject
    public TracksProgressViewModel(@NonNull Application application,
                                   UserProgressRepository userProgressRepository,
                                   MilestoneRepository milestoneRepository,
                                   UserSettingsRepository userSettingsRepository,
                                   RouteService routeService) {
        super(application, userSettingsRepository);
        this.userProgressRepository = userProgressRepository;
        this.milestoneRepository = milestoneRepository;
        this.routeService = routeService;
        allProgress = userProgressRepository.getProgressWithMilestonesForStatusWithSeparators(
                userSettingsRepository.getShowCompletedTracks()
        );
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
        MutableLiveData<MethodResult> result = new MutableLiveData<>();
        userProgressRepository.finishProgress(progressId).observeForever(new Observer<>() {
            @Override
            public void onChanged(MethodResult methodResult) {
                if (methodResult.status == ResultStatus.SUCCESS) {
                    _methodResult.postValue(new Event<>(methodResult));
                } else {
                    Log.e("TracksProgressViewModel", "Error finishing progress: " + methodResult.message);
                }
                result.removeObserver(this); // very important to prevent leaks
            }
        });
    }

    public void calculateAndPostPosition(UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<GeoPoint> geoData = routeService.getGeoData(userProgressWithTrackAndMilestones);
                _geoData.postValue(new Event<>(geoData));
                GeoPoint positionForDistanceWalked = routeService.getPosition(geoData, userProgressWithTrackAndMilestones.userProgress.distanceWalked);
                Log.d("TracksProgressViewModel", "Position for distance walked: " + positionForDistanceWalked);
                _pos.postValue(new Event<>(positionForDistanceWalked));
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

}