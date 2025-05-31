package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.UserProgressWithTrack;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;

@Dao
public interface UserProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertProgress(UserProgress progress);

    @Query("SELECT * FROM user_progress WHERE trackId = :trackId")
    UserProgress getProgressForTrack(long trackId);

    @Query("SELECT * FROM user_progress WHERE trackId = :trackId")
    LiveData<List<UserProgress>> getProgressForTrackLive(long trackId);

    @Query("SELECT * FROM user_progress WHERE status = 'active' OR status = 'paused' ORDER BY status DESC, stepsWalked DESC")
    LiveData<List<UserProgress>> getProgressesLive();

    @Query("UPDATE user_progress SET stepsWalked = :steps WHERE trackId = :trackId")
    void updateSteps(int trackId, int steps);

    @Transaction
    @Query("SELECT * FROM user_progress WHERE status IN ('paused', 'active') ORDER BY status DESC, stepsWalked DESC")
    List<UserProgressWithTrack> getActiveOrPausedProgress();
    @Transaction
    @Query("SELECT * FROM user_progress WHERE status IN ('paused', 'active') ORDER BY status DESC, stepsWalked DESC")
    LiveData<List<UserProgressWithTrack>> getActiveOrPausedProgressLive();

    @Transaction
    @Query("SELECT * FROM user_progress WHERE status IN ('paused', 'active')")
    LiveData<List<UserProgressWithTrackAndMilestones>> getActiveOrPausedProgressWithMilestones();
}