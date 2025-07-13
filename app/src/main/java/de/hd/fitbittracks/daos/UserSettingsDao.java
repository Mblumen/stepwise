package de.hd.fitbittracks.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import de.hd.fitbittracks.entities.UserSettings;

@Dao
public interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    LiveData<UserSettings> getSettingsLive();

    @Query("SELECT * FROM user_settings WHERE id = 1")
    UserSettings getSettings();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserSettings settings);

    @Query("SELECT showCompletedTracks FROM user_settings WHERE id = 1")
    LiveData<Boolean> getShowCompletedTracks();

    @Query("SELECT stepLengthInMeters FROM user_settings WHERE id = 1")
    LiveData<Float> getStepLength();
}