package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.pojos.TrackWithMilestones;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;

@Dao
public interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTrack(Track track);

    @Query("SELECT * FROM tracks")
    List<Track> getAllTracks();

    @Query("SELECT * FROM tracks")
    LiveData<List<Track>> getAllTracksLive();

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    Track getTrackById(int trackId);

    @Query("SELECT * FROM tracks WHERE name like :trackName")
    Track getTrackByName(String trackName);

    @Delete
    void deleteTrack(Track track);

    @Transaction
    @Query("SELECT * FROM tracks")
    LiveData<List<TrackWithMilestones>> getAllTracksWithMilestones();
}