package de.hd.fitbittracks.ui.tracksprogress;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.repositories.MilestoneRepository;
import de.hd.fitbittracks.repositories.UserProgressRepository;

public class TracksProgressViewModel extends AndroidViewModel {

    private final LiveData<List<ListItem>> allProgress;
    private final MilestoneRepository repository;
    private Track track;
    private int stepsWalked;

    public TracksProgressViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        UserProgressRepository userProgressRepository = new UserProgressRepository(db.userProgressDao(), db.trackDao());
        allProgress = userProgressRepository.getProgressWithMilestonesForStatusWithSeparators();
        //allProgress = db.userProgressDao().getProgressWithTrackAndMilestonesForStatus(ProgressStatus.ACTIVE, ProgressStatus.PAUSED);
        repository = new MilestoneRepository(db.milestoneDao());
    }

    public void setTrack(Track track) {
        this.track = track;
    }
    public void setStepsWalked(int stepsWalked) {
        this.stepsWalked = stepsWalked;
    }
    public LiveData<List<MilestoneWithStatus>> getAllMilestones() { return repository.getAllMilestonesByTrack(track.id, stepsWalked); }
    public LiveData<List<ListItem>> getAllProgress() {
        return allProgress;
    }
}