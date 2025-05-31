package de.hd.fitbittracks.ui.tracksprogress;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;
import de.hd.fitbittracks.pojos.UserProgressWithTrack;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.repositories.MilestoneRepository;
import de.hd.fitbittracks.repositories.TrackRepository;
import de.hd.fitbittracks.repositories.UserProgressRepository;

public class TracksProgressViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final LiveData<List<UserProgressWithTrackAndMilestones>> allProgress;
    private final MilestoneRepository repository;

    private Track track;
    private int stepsWalked;

    public TracksProgressViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        allProgress = db.userProgressDao().getActiveOrPausedProgressWithMilestones();
        repository = new MilestoneRepository(db.milestoneDao());
    }

    public void setTrack(Track track) {
        this.track = track;
    }
    public void setStepsWalked(int stepsWalked) {
        this.stepsWalked = stepsWalked;
    }
    public LiveData<List<MilestoneWithStatus>> getAllMilestones() { return repository.getAllMilestonesByTrack(track.id, stepsWalked); }
    public LiveData<List<UserProgressWithTrackAndMilestones>> getAllProgress() {
        return allProgress;
    }

    public int getActiveProgressPosition() {
        List<UserProgressWithTrack> activeOrPausedProgress = db.userProgressDao().getActiveOrPausedProgress();
        int index = -1;
        for(int i = 0; i < activeOrPausedProgress.size(); i++) {
            UserProgressWithTrack progress = activeOrPausedProgress.get(i);
            if (progress.userProgress.status == ProgressStatus.ACTIVE) {
                index = i;
                break;
            }
        }
        return index;
    }


}