package de.hd.stepwise.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Upsert;

import java.util.List;

import de.hd.stepwise.entities.UserProgress;
import de.hd.stepwise.entities.UserProgressMilestoneStatus;
import de.hd.stepwise.enums.ProgressStatus;
import de.hd.stepwise.pojos.UserProgressWithTrackAndMilestones;

@Dao
public interface UserProgressDao {

    @Upsert
    void insertUserProgress(UserProgress progress);

    @Query("SELECT * FROM user_progress WHERE trackId = :trackId")
    UserProgress getProgressForTrack(long trackId);

    //get by id
    @Query("SELECT * FROM user_progress WHERE id = :id")
    UserProgress getProgressById(long id);

    @Query("SELECT * FROM user_progress WHERE trackId = :trackId AND status IN (:status)")
    UserProgress getProgressForTrackAndStatus(long trackId, ProgressStatus... status);

    @Query("SELECT * FROM user_progress WHERE status IN (:status)")
    UserProgress getProgressForStatus(ProgressStatus... status);

    @Query("Update user_progress SET status = 'paused' WHERE status = 'active'")
    void pauseActiveTrack();

    @Query("Update user_progress SET status = :status WHERE trackId = :trackId")
    void updateStatus(long trackId, ProgressStatus status);

    @Query("UPDATE user_progress SET stepsWalked = :steps WHERE trackId = :trackId")
    void updateSteps(int trackId, int steps);

    @Transaction
    @Query("SELECT * FROM user_progress WHERE status IN (:status) ORDER BY status ASC, stepsWalked DESC")
    LiveData<List<UserProgressWithTrackAndMilestones>> getProgressWithTrackAndMilestonesForStatus(List<ProgressStatus> status);

    @Query("SELECT * FROM user_progress WHERE status = 'active'")
    UserProgress getActiveUserProgress();

    @Query("SELECT milestoneId FROM user_progress_milestone_status WHERE progressId = :progressId")
    List<Long>getNotifiedMilestonesForProgress(long progressId);

    @Query("SELECT * FROM user_progress_milestone_status")
    List<UserProgressMilestoneStatus>getNotifiedMilestones();

    //get UserProgressMilestoneStatus for a specific progressId and milestoneId
    @Query("SELECT * FROM user_progress_milestone_status WHERE progressId = :progressId AND milestoneId = :milestoneId")
    UserProgressMilestoneStatus getMilestoneStatusForProgress(long progressId, long milestoneId);

    @Query("SELECT * FROM user_progress_milestone_status WHERE progressId = :progressId")
    LiveData<List<UserProgressMilestoneStatus>> getMilestoneStatusesForProgress(long progressId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void markMilestoneNotified(UserProgressMilestoneStatus status);
}