package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.hd.fitbittracks.entities.Milestone;

@Dao
public interface MilestoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMilestones(List<Milestone> milestones);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMilestone(Milestone milestone);

    @Query("SELECT * FROM milestones WHERE id = :milestoneId")
    LiveData<Milestone> getMilestoneById(long milestoneId);

    @Query("SELECT * FROM milestones ORDER BY trackId")
    LiveData<List<Milestone>> getAllMilestones();
    @Query("SELECT * FROM milestones WHERE trackId = :trackId ORDER BY distanceOffset ASC")
    List<Milestone> getMilestonesForTrack(long trackId);

    @Query("SELECT * FROM milestones WHERE trackId = :trackId ORDER BY distanceOffset ASC")
    LiveData<List<Milestone>> getMilestonesForTrackLive(long trackId);

    //unlock milestone by id
    @Query("UPDATE milestones SET unlocked = 1 WHERE id = :milestoneId")
    void unlockMilestone(long milestoneId);
}