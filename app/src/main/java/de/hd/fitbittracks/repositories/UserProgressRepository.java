package de.hd.fitbittracks.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

import de.hd.fitbittracks.daos.TrackDao;
import de.hd.fitbittracks.daos.UserProgressDao;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.entities.UserProgressMilestoneStatus;
import de.hd.fitbittracks.enums.ResultStatus;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.MethodResultWithData;
import de.hd.fitbittracks.pojos.Pair;
import de.hd.fitbittracks.pojos.Separator;
import de.hd.fitbittracks.pojos.TrackWithMilestones;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;

public class UserProgressRepository extends BaseRepository{
    private final UserProgressDao userProgressDao;
    private final TrackDao trackDao;
    public UserProgressRepository(UserProgressDao userProgressDao, TrackDao trackDao) {
        this.userProgressDao = userProgressDao;
        this.trackDao = trackDao;
    }

    public LiveData<List<ListItem>> getProgressWithMilestonesForStatusWithSeparators() {
        return Transformations.map(
            userProgressDao.getProgressWithTrackAndMilestonesForStatus(ProgressStatus.ACTIVE, ProgressStatus.PAUSED, ProgressStatus.COMPLETED),
            progressesWithTrackAndMileStones -> {
                List<ListItem> result = new ArrayList<>();
                List<UserProgressWithTrackAndMilestones> active = new ArrayList<>();
                List<UserProgressWithTrackAndMilestones> completed = new ArrayList<>();

                for (UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones : progressesWithTrackAndMileStones) {
                    if (userProgressWithTrackAndMilestones.userProgress.status == ProgressStatus.ACTIVE || userProgressWithTrackAndMilestones.userProgress.status == ProgressStatus.PAUSED) {
                        active.add(userProgressWithTrackAndMilestones);
                    } else if (userProgressWithTrackAndMilestones.userProgress.status == ProgressStatus.COMPLETED) {
                        completed.add(userProgressWithTrackAndMilestones);
                    }
                }
                if (!active.isEmpty()) {
                    result.add(new Separator("Current Tracks"));
                    result.addAll(active);
                }
                if (!completed.isEmpty()) {
                    result.add(new Separator("Completed Tracks"));
                    result.addAll(completed);
                }
                return result;
            }
        );
    }


    public LiveData<MethodResult> startTrack(long trackId) {
        MutableLiveData<MethodResult> result = new MutableLiveData<>();
        // This method is used to start tracking progress for a specific track.
        // It updates the status of the user progress to 'active' for the given track ID.
        // If the track is already being tracked, this will simply update the status to 'active'.
        // It sets the status of all other active Userprogress entries to 'paused'
        // First checks whether an active or paused progress exists for the track
        executor.execute(() -> {
            UserProgress existingProgress = userProgressDao.getProgressForStatus(ProgressStatus.ACTIVE);
            if (existingProgress != null) {
                if(existingProgress.trackId == trackId) {
                    // If the existing active progress is for the same track, just notify the user, that the track is already active
                    result.postValue(new MethodResult(ResultStatus.SUCCESS, "Track is already active."));
                    return;
                }
                // If the existing progress is for a different track, pause it
                userProgressDao.pauseActiveTrack();
                // Now check if there is a progress for the given track
                UserProgress trackProgress = userProgressDao.getProgressForTrackAndStatus(trackId, ProgressStatus.ACTIVE, ProgressStatus.PAUSED);
                if (trackProgress != null) {
                    // If there is already a progress for the track, update its status to active
                    trackProgress.status = ProgressStatus.ACTIVE;
                    userProgressDao.insertUserProgress(trackProgress);
                    result.postValue(new MethodResult(ResultStatus.SUCCESS, "Track status updated to active."));
                    return;
                }
            }
            // Create a new UserProgress entry for the track
            UserProgress newProgress = new UserProgress();
            newProgress.trackId = trackId;
            newProgress.stepsWalked = 0; // Initialize steps walked to 0
            newProgress.status = ProgressStatus.ACTIVE; // Set status to active
            // Insert the new UserProgress entry into the database
            userProgressDao.insertUserProgress(newProgress);
            // Post a success result
            result.postValue(new MethodResult(ResultStatus.SUCCESS, "Track started successfully."));
            return;
        });
        return result;
    }

    @Transaction
    public MethodResultWithData<Pair<UserProgress, Milestone>> updateStepsWalked(int stepsWalked) {
        UserProgress progress = userProgressDao.getActiveUserProgress();
        if (progress != null) {
            int totalSteps = progress.stepsWalked + stepsWalked;
            progress.stepsWalked = totalSteps;
            userProgressDao.insertUserProgress(progress);
            TrackWithMilestones trackWithMilestonesById = trackDao.getTrackWithMilestonesById(progress.trackId);
            List<Long> notifiedMilestones = userProgressDao.getNotifiedMilestonesForProgress(progress.id);
            List<Milestone> milestones = trackWithMilestonesById.milestones;
            for (Milestone m : milestones) {
                if (totalSteps >= m.stepOffset && !notifiedMilestones.contains(m.id)) {
                    UserProgressMilestoneStatus userProgressMilestoneStatus = new UserProgressMilestoneStatus(progress.id, m.id, true);
                    userProgressDao.markMilestoneNotified(userProgressMilestoneStatus);
                    return new MethodResultWithData<>(ResultStatus.SUCCESS, "Milestone reached: " + m.title, new Pair<>(progress, m));
                }
            }
        }
        return null;
    }
}
