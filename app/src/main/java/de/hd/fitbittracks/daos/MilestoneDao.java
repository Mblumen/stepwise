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
    void insertMilestone(Milestone milestone);

    @Query("SELECT * FROM milestones WHERE id = :milestoneId")
    LiveData<Milestone> getMilestoneById(long milestoneId);

    @Query("SELECT * FROM milestones ORDER BY trackId")
    LiveData<List<Milestone>> getAllMilestones();
    @Query("SELECT * FROM milestones WHERE trackId = :trackId ORDER BY stepOffset ASC")
    List<Milestone> getMilestonesForTrack(long trackId);

    @Query("SELECT * FROM milestones WHERE trackId = :trackId ORDER BY stepOffset ASC")
    LiveData<List<Milestone>> getMilestonesForTrackLive(long trackId);
}