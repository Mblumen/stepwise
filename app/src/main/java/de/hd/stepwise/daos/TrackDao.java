package de.hd.stepwise.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Upsert;

import java.util.List;

import de.hd.stepwise.entities.Track;
import de.hd.stepwise.pojos.TrackRoute;
import de.hd.stepwise.pojos.TrackWithMilestones;

@Dao
public interface TrackDao {

    @Upsert
    long insertTrack(Track track);

    @Query("Update track SET localImagePath = :localImagePath WHERE id = :trackId")
    void updateLocalImagePath(long trackId, String localImagePath);

    @Query("Update track SET trackRoute = :trackRoute WHERE id = :trackId")
    void updateTrackRoute(long trackId, TrackRoute trackRoute);
    @Query("SELECT * FROM track")
    List<Track> getAllTracks();

    @Query("SELECT * FROM track")
    LiveData<List<Track>> getAllTracksLive();

    @Query("SELECT * FROM track WHERE id = :trackId")
    Track getTrackById(int trackId);

    @Query("SELECT * FROM track WHERE name like :trackName")
    Track getTrackByName(String trackName);

    @Delete
    void deleteTrack(Track track);

    @Transaction
    @Query("SELECT * FROM track")
    LiveData<List<TrackWithMilestones>> getAllTracksWithMilestones();

    @Transaction
    @Query("SELECT * FROM track WHERE id = :trackId")
    TrackWithMilestones getTrackWithMilestonesById(long trackId);

    @Query("SELECT COUNT(*) FROM track")
    int trackCount();
}