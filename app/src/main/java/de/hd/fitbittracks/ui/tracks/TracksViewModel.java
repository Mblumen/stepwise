package de.hd.fitbittracks.ui.tracks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.pojos.events.Event;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.TrackWithMilestones;
import de.hd.fitbittracks.repositories.TrackRepository;
import de.hd.fitbittracks.repositories.UserProgressRepository;
import de.hd.fitbittracks.repositories.UserSettingsRepository;
import de.hd.fitbittracks.ui.BaseViewModel;

@HiltViewModel
public class TracksViewModel extends BaseViewModel {

    private final MilestoneDao milestoneDao;
    private final LiveData<List<TrackWithMilestones>> allTracks;
    private final UserProgressRepository userProgressRepository;
    private long trackId;
    private final MutableLiveData<Event<MethodResult>> _methodResult = new MutableLiveData<>();
    public LiveData<Event<MethodResult>> observedResult = _methodResult;

    @Inject
    public TracksViewModel(@NonNull Application application, UserProgressRepository userProgressRepository, TrackRepository trackRepository, UserSettingsRepository userSettingsRepository) {
        super(application, userSettingsRepository);
        AppDatabase db = AppDatabase.getInstance(application);
        allTracks = trackRepository.getSortedTracksWithMilestones();
        this.userProgressRepository = userProgressRepository;
        milestoneDao = db.milestoneDao();
    }

    public LiveData<List<TrackWithMilestones>> getAllTracks() { return allTracks; }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }
    public LiveData<List<MilestoneWithTotalDistance>> getAllMilestones() { return milestoneDao.getMilestonesForTrackLive(trackId); }

    public void selectTrack(long trackId) {
        LiveData<MethodResult> resultLiveData =  userProgressRepository.startTrack(trackId);
        resultLiveData.observeForever(new Observer<>() {
            @Override
            public void onChanged(MethodResult result) {
                _methodResult.postValue(new Event<>(result));
                resultLiveData.removeObserver(this); // very important to prevent leaks
            }
        });
    }
}