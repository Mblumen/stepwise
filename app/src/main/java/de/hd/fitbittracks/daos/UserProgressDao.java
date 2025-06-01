package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;

@Dao
public interface UserProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserProgress(UserProgress progress);

    @Query("SELECT * FROM user_progress WHERE trackId = :trackId")
    UserProgress getProgressForTrack(long trackId);

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
    LiveData<List<UserProgressWithTrackAndMilestones>> getProgressWithTrackAndMilestonesForStatus(ProgressStatus... status);
}