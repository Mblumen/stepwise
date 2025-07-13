package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;

@Dao
public interface MilestoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMilestones(List<Milestone> milestones);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMilestone(Milestone milestone);

    @Query("SELECT * FROM milestonewithtotaldistance WHERE id = :milestoneId")
    LiveData<MilestoneWithTotalDistance> getMilestoneById(long milestoneId);

    @Query("SELECT * FROM milestones ORDER BY trackId")
    LiveData<List<Milestone>> getAllMilestones();
    @Query("SELECT * FROM milestones WHERE trackId = :trackId ORDER BY id ASC")
    List<Milestone> getMilestonesForTrack(long trackId);

    @Query("SELECT * FROM milestonewithtotaldistance WHERE trackId = :trackId ORDER BY id ASC")
    LiveData<List<MilestoneWithTotalDistance>> getMilestonesForTrackLive(long trackId);

    //unlock milestone by id
    @Query("UPDATE milestones SET unlocked = 1 WHERE id = :milestoneId")
    void unlockMilestone(long milestoneId);

}