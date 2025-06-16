package de.hd.fitbittracks.ui.tracksprogress;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;

import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.Event;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.repositories.MilestoneRepository;
import de.hd.fitbittracks.repositories.UserProgressRepository;
import de.hd.fitbittracks.repositories.UserSettingsRepository;
import de.hd.fitbittracks.ui.BaseViewModel;

public class TracksProgressViewModel extends BaseViewModel {

    private final LiveData<List<ListItem>> allProgress;
    private final MilestoneRepository repository;

    private final UserProgressRepository userProgressRepository;
    private final UserSettingsRepository userSettingsRepository;
    private Track track;
    private float distanceWalked;
    private int stepsWalked;

    public TracksProgressViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        userProgressRepository = new UserProgressRepository(db.userProgressDao(), db.trackDao(), db.userSettingsDao());
        userSettingsRepository = new UserSettingsRepository(application);
        allProgress = userProgressRepository.getProgressWithMilestonesForStatusWithSeparators(
                userSettingsRepository.getShowCompletedTracks()
        );
        repository = new MilestoneRepository(db.milestoneDao());
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
    public LiveData<List<MilestoneWithStatus>> getAllMilestones() { return repository.getAllMilestonesByTrack(track.id, distanceWalked, stepsWalked); }
    public LiveData<List<ListItem>> getAllProgress() {
        return allProgress;
    }
}