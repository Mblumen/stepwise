package de.hd.fitbittracks.ui.activetracks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.repositories.TrackRepository;

public class ActiveTracksViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final LiveData<List<Track>> allTracks;
    private final TrackRepository repository;

    private long trackId;


    public ActiveTracksViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        allTracks = db.trackDao().getAllTracksLive();
        repository = new TrackRepository(db.milestoneDao());
    }

    public LiveData<List<Track>> getAllTracks() { return allTracks; }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }
    public LiveData<List<Milestone>> getAllMilestones() { return db.milestoneDao().getMilestonesForTrackLive(trackId); }

    public LiveData<Map<Long, List<Milestone>>> getAllMilestonesByTrack() {
        return repository.getAllMilestonesByTrack();
    }
}