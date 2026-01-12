package de.hd.stepwise.ui.tracks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.stepwise.daos.MilestoneDao;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.pojos.events.Event;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.pojos.TrackWithMilestones;
import de.hd.stepwise.repositories.MilestoneRepository;
import de.hd.stepwise.repositories.TrackRepository;
import de.hd.stepwise.repositories.UserProgressRepository;
import de.hd.stepwise.repositories.UserSettingsRepository;

@HiltViewModel
public class TracksViewModel extends BaseTracksViewModel {

    private final MilestoneDao milestoneDao;
    private final LiveData<List<TrackWithMilestones>> allTracks;
    private final UserProgressRepository userProgressRepository;
    private long trackId;
    private final MutableLiveData<Event<MethodResult>> _methodResult = new MutableLiveData<>();
    public LiveData<Event<MethodResult>> observedResult = _methodResult;
    protected final LiveData<Boolean> showLockedMilestones;
    public int expandedPosition = -1;

    @Inject
    public TracksViewModel(@NonNull Application application, UserProgressRepository userProgressRepository, TrackRepository trackRepository, UserSettingsRepository userSettingsRepository, MilestoneRepository milestoneRepository) {
        super(application, userSettingsRepository, trackRepository, milestoneRepository);
        allTracks = trackRepository.getSortedTracksWithMilestones();
        this.userProgressRepository = userProgressRepository;
        milestoneDao = db.milestoneDao();
        showLockedMilestones = userSettingsRepository.getShowLockedMilestones();

    }

    public LiveData<List<TrackWithMilestones>> getAllTracks() { return allTracks; }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }
    public LiveData<List<MilestoneWithTotalDistance>> getAllMilestones() { return milestoneDao.getMilestonesForTrackLive(trackId); }
    public LiveData<Boolean> getShowLockedMilestones() { return showLockedMilestones; }
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