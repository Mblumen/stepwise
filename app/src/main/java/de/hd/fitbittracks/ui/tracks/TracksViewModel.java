package de.hd.fitbittracks.ui.tracks;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.Map;

import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserSettings;
import de.hd.fitbittracks.pojos.Event;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.TrackWithMilestones;
import de.hd.fitbittracks.repositories.TrackRepository;
import de.hd.fitbittracks.repositories.UserProgressRepository;
import de.hd.fitbittracks.repositories.UserSettingsRepository;
import de.hd.fitbittracks.ui.BaseViewModel;

public class TracksViewModel extends BaseViewModel {

    private final MilestoneDao milestoneDao;
    private final LiveData<List<TrackWithMilestones>> allTracks;
    private final TrackRepository repository;
    private final UserProgressRepository userProgressRepository;
    private long trackId;



    private final MutableLiveData<Event<MethodResult>> _methodResult = new MutableLiveData<>();
    public LiveData<Event<MethodResult>> observedResult = _methodResult;

    public TracksViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        allTracks = db.trackDao().getAllTracksWithMilestones();
        repository = new TrackRepository(db.milestoneDao());
        userProgressRepository = new UserProgressRepository(db.userProgressDao(), db.trackDao(), db.userSettingsDao());
        milestoneDao = db.milestoneDao();
    }

    public LiveData<List<TrackWithMilestones>> getAllTracks() { return allTracks; }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }
    public LiveData<List<Milestone>> getAllMilestones() { return milestoneDao.getMilestonesForTrackLive(trackId); }

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