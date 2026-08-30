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
import de.hd.stepwise.entities.ReachedMilestone;
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

    @Transaction
    @Query("SELECT * FROM user_progress WHERE id = :progressId")
    UserProgressWithTrackAndMilestones getProgressWithTrackAndMilestonesById(long progressId);

    @Transaction
    @Query("SELECT * FROM user_progress WHERE id = :progressId")
    LiveData<UserProgressWithTrackAndMilestones> observeProgressWithTrackAndMilestonesById(long progressId);

    @Transaction
    @Query("SELECT * FROM user_progress WHERE status IN ('active', 'paused') "
            + "ORDER BY CASE status WHEN 'active' THEN 0 ELSE 1 END LIMIT 1")
    UserProgressWithTrackAndMilestones getCurrentProgressWithTrackAndMilestones();

    @Transaction
    @Query("SELECT * FROM user_progress WHERE status IN ('active', 'paused') "
            + "ORDER BY CASE status WHEN 'active' THEN 0 ELSE 1 END LIMIT 1")
    LiveData<UserProgressWithTrackAndMilestones> observeCurrentProgressWithTrackAndMilestones();

    @Query("SELECT * FROM user_progress WHERE status = 'active'")
    UserProgress getActiveUserProgress();

    @Query("SELECT milestoneId FROM reached_milestone WHERE progressId = :progressId")
    List<Long> getReachedMilestoneIdsForProgress(long progressId);

    @Query("SELECT COUNT(DISTINCT milestoneId) FROM reached_milestone")
    int countDistinctReachedMilestones();

    @Query("SELECT COUNT(DISTINCT trackId) FROM user_progress WHERE status = :status")
    int countDistinctTracksWithStatus(ProgressStatus status);

    @Query("SELECT COALESCE(SUM(stepsWalked), 0) FROM user_progress")
    int getTotalCreditedSteps();

    @Query("SELECT COALESCE(SUM(distanceWalked), 0) FROM user_progress")
    float getTotalCreditedDistance();

    @Query("SELECT * FROM reached_milestone WHERE progressId = :progressId AND milestoneId = :milestoneId")
    ReachedMilestone getReachedMilestone(long progressId, long milestoneId);

    @Query("SELECT * FROM reached_milestone WHERE progressId = :progressId AND milestoneId = :milestoneId")
    LiveData<ReachedMilestone> observeReachedMilestone(long progressId, long milestoneId);

    @Query("SELECT * FROM reached_milestone WHERE progressId = :progressId")
    LiveData<List<ReachedMilestone>> observeReachedMilestonesForProgress(long progressId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReachedMilestone(ReachedMilestone reachedMilestone);

    @Query("UPDATE reached_milestone SET selectedQuizAnswer = :selectedAnswer, "
            + "quizCompletedAt = :completedAt "
            + "WHERE progressId = :progressId AND milestoneId = :milestoneId")
    void updateReachedMilestoneQuizState(long progressId, long milestoneId,
                                         int selectedAnswer, Long completedAt);
}
