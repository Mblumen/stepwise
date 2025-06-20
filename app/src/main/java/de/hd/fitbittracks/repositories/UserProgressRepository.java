package de.hd.fitbittracks.repositories;

import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

import de.hd.fitbittracks.daos.AchievementDao;
import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.daos.TrackDao;
import de.hd.fitbittracks.daos.UserProgressDao;
import de.hd.fitbittracks.daos.UserSettingsDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.entities.UserProgressMilestoneStatus;
import de.hd.fitbittracks.enums.AchievementType;
import de.hd.fitbittracks.enums.ResultStatus;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.pojos.Separator;
import de.hd.fitbittracks.pojos.TrackWithMilestones;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.pojos.events.AchievementEvent;
import de.hd.fitbittracks.pojos.events.MilestoneWithProgressEvent;
import de.hd.fitbittracks.pojos.events.TrackWithProgressEvent;

public class UserProgressRepository extends BaseRepository{
    private final UserProgressDao userProgressDao;
    private final TrackDao trackDao;
    private final MilestoneDao milestoneDao;
    private final UserSettingsDao userSettingsDao;
    private final AchievementDao achievementDao;
    private final MutableLiveData<MilestoneWithProgressEvent> milestoneWithProgressEvents = new MutableLiveData<>();
    private final MutableLiveData<AchievementEvent> achievementEvents = new MutableLiveData<>();
    private final MutableLiveData<TrackWithProgressEvent> trackWithProgressEvents = new MutableLiveData<>();

    public UserProgressRepository(AppDatabase appDatabase) {
        this.userProgressDao = appDatabase.userProgressDao();
        this.trackDao = appDatabase.trackDao();
        this.milestoneDao = appDatabase.milestoneDao();
        this.userSettingsDao = appDatabase.userSettingsDao();
        this.achievementDao = appDatabase.achievementDao();
    }

    public LiveData<MilestoneWithProgressEvent> getMilestoneProgressEvents() {
        return milestoneWithProgressEvents;
    }
    public LiveData<AchievementEvent> getAchievementEvents() {
        return achievementEvents;
    }

    public LiveData<TrackWithProgressEvent> getTrackWithProgressEvents() {
        return trackWithProgressEvents;
    }

    public LiveData<List<ListItem>> getProgressWithMilestonesForStatusWithSeparators(LiveData<Boolean> includeCompletedLiveData) {
        return Transformations.switchMap(includeCompletedLiveData, includeCompleted -> {
            List<ProgressStatus> statuses = new ArrayList<>();
            statuses.add(ProgressStatus.ACTIVE);
            statuses.add(ProgressStatus.PAUSED);
            if (Boolean.TRUE.equals(includeCompleted)) {
                statuses.add(ProgressStatus.COMPLETED);
            }

            return Transformations.map(
                userProgressDao.getProgressWithTrackAndMilestonesForStatus(statuses),
                progressesWithTrackAndMileStones -> {
                    Log.e("UserProgressRepository", "getProgressWithMilestonesForStatusWithSeparators: " + progressesWithTrackAndMileStones.size());
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
        });
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
    public void updateStepsWalked(int stepsWalked) {
        UserProgress progress = userProgressDao.getActiveUserProgress();
        if (progress != null) {
            //distance walked
            float stepLength = userSettingsDao.getSettings().stepLengthInMeters;
            float distanceWalked = stepsWalked * stepLength;

            int totalSteps = progress.stepsWalked + stepsWalked;
            float totalDistance = progress.distanceWalked + distanceWalked;
            progress.distanceWalked = totalDistance;
            progress.stepsWalked = totalSteps;
            userProgressDao.insertUserProgress(progress);

            //update achievements
            List<Achievement> achievementsByType = achievementDao.getAchievementsByType(AchievementType.DISTANCE);
            updateDistanceAchievements(achievementsByType, distanceWalked);

            TrackWithMilestones trackWithMilestonesById = trackDao.getTrackWithMilestonesById(progress.trackId);
            List<Long> notifiedMilestones = userProgressDao.getNotifiedMilestonesForProgress(progress.id);
            List<Milestone> milestones = trackWithMilestonesById.milestones;
            for (Milestone m : milestones) {
                if (totalDistance >= m.distanceOffset && !notifiedMilestones.contains(m.id)) {
                    UserProgressMilestoneStatus userProgressMilestoneStatus = new UserProgressMilestoneStatus(progress.id, m.id, true, totalSteps);
                    userProgressDao.markMilestoneNotified(userProgressMilestoneStatus);
                    milestoneDao.unlockMilestone(m.id);
                    //check if it was the last milestone (highest distance offset)
                    boolean isLastMilestone = true;
                    for (Milestone other : milestones) {
                        if (other.distanceOffset > m.distanceOffset) {
                            isLastMilestone = false;
                            break;
                        }
                    }
                    if (isLastMilestone) {
                        trackWithProgressEvents.postValue(new TrackWithProgressEvent(new Pair<>(trackWithMilestonesById.track, progress), "Track completed: " + trackWithMilestonesById.track.name));
                    }
                    // Post the milestone event
                    milestoneWithProgressEvents.postValue(new MilestoneWithProgressEvent(new Pair<>(m, progress), "Milestone reached: " + m.title));
                }
            }
        }

    }

    private void updateDistanceAchievements(List<Achievement> achievements, float distanceWalked) {
        for (Achievement achievement : achievements) {
            if (achievement.unlocked) continue; // Skip already unlocked achievements
            achievement.progressValue += distanceWalked;
            if(achievement.progressValue > achievement.targetValue) {
                // If the progress exceeds the target value, set it to the target value
                achievement.progressValue = achievement.targetValue;
            }
            if (achievement.progressValue >= achievement.targetValue) {
                achievement.unlocked = true;
                achievement.dateUnlocked = System.currentTimeMillis();
                // Post the achievement event
                achievementEvents.postValue(new AchievementEvent(achievement, "Achievement unlocked: " + achievement.title));
            }
            achievementDao.update(achievement);
        }
    }

    public LiveData<MethodResult> pauseTrackProgress(long progressId) {
        // This method is used to pause the progress for a specific track.
        // It updates the status of the user progress to 'paused' for the given track ID.
        MutableLiveData<MethodResult> result = new MutableLiveData<>();
        executor.execute(() -> {
            UserProgress progress = userProgressDao.getProgressById(progressId);
            if (progress != null) {
                progress.status = ProgressStatus.PAUSED;
                userProgressDao.insertUserProgress(progress);
                result.postValue(new MethodResult(ResultStatus.SUCCESS, "Track progress paused successfully."));
            } else {
                result.postValue(new MethodResult(ResultStatus.ERROR, "Track progress not found."));
            }
        });
        return result;
    }

    public LiveData<MethodResult> resumeTrackProgress(long progressId) {
        // This method is used to resume the progress for a specific track.
        // It updates the status of the user progress to 'active' for the given track ID.
        MutableLiveData<MethodResult> result = new MutableLiveData<>();
        executor.execute(() -> {
            UserProgress activeProgress = userProgressDao.getActiveUserProgress();
            UserProgress progress = userProgressDao.getProgressById(progressId);
            if (activeProgress != null && activeProgress.id != progressId) {
                // If there is already an active progress, pause it
                activeProgress.status = ProgressStatus.PAUSED;
                userProgressDao.insertUserProgress(activeProgress);
            }
            if (progress != null) {
                progress.status = ProgressStatus.ACTIVE;
                userProgressDao.insertUserProgress(progress);
                result.postValue(new MethodResult(ResultStatus.SUCCESS, "Track progress resumed successfully."));
            } else {
                result.postValue(new MethodResult(ResultStatus.ERROR, "Track progress not found."));
            }
        });
        return result;
    }

    public LiveData<MethodResult> finishProgress(long progressId) {
        // This method is used to finish the progress for a specific track.
        // It updates the status of the user progress to 'completed' for the given track ID.
        MutableLiveData<MethodResult> result = new MutableLiveData<>();
        executor.execute(() -> {
            UserProgress progress = userProgressDao.getProgressById(progressId);
            if (progress != null) {
                if(progress.status == ProgressStatus.COMPLETED) {
                    result.postValue(new MethodResult(ResultStatus.SUCCESS, "Track progress already completed."));
                    return;
                }
                progress.status = ProgressStatus.COMPLETED;
                userProgressDao.insertUserProgress(progress);
                result.postValue(new MethodResult(ResultStatus.SUCCESS, "Track progress finished successfully."));
            } else {
                result.postValue(new MethodResult(ResultStatus.ERROR, "Track progress not found."));
            }
        });
        return result;
    }

}
