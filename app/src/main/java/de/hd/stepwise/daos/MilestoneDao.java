package de.hd.stepwise.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import de.hd.stepwise.entities.Milestone;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.pojos.MilestoneImage;

@Dao
public interface MilestoneDao {

    @Upsert
    void insertMilestone(Milestone milestone);

    @Query("Update milestone SET localImagePath = :localImagePath WHERE id = :milestoneId")
    void updateLocalImagePath(long milestoneId, String localImagePath);

    @Query("Update milestone SET extra_images = :images WHERE id = :milestoneId")
    void updateExtraImages(long milestoneId, List<MilestoneImage> images);

    @Query("SELECT * FROM milestonewithtotaldistance WHERE id = :milestoneId")
    MilestoneWithTotalDistance getMilestoneById(long milestoneId);
    @Query("SELECT * FROM milestonewithtotaldistance WHERE id = :milestoneId")
    LiveData<MilestoneWithTotalDistance> getMilestoneByIdLive(long milestoneId);

    @Query("SELECT * FROM milestone ORDER BY trackId")
    LiveData<List<Milestone>> getAllMilestones();
    @Query("SELECT * FROM milestone WHERE trackId = :trackId ORDER BY id ASC")
    List<Milestone> getMilestonesForTrack(long trackId);

    @Query("SELECT * FROM milestonewithtotaldistance WHERE trackId = :trackId ORDER BY id ASC")
    LiveData<List<MilestoneWithTotalDistance>> getMilestonesForTrackLive(long trackId);

    @Query("SELECT * FROM milestone WHERE title = :title AND trackId = :trackId LIMIT 1")
    Milestone getMilestoneByTitleAndTrackId(String title, long trackId);
    //unlock milestone by id
    @Query("UPDATE milestone SET unlocked = 1 WHERE id = :milestoneId")
    void unlockMilestone(long milestoneId);

}