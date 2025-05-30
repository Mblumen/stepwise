package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;

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
}