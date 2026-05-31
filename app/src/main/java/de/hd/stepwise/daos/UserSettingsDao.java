package de.hd.stepwise.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.enums.StepSource;

@Dao
public interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    LiveData<UserSettings> getSettingsLive();

    @Query("SELECT * FROM user_settings WHERE id = 1")
    UserSettings getSettings();

    @Upsert
    void insertOrUpdate(UserSettings settings);

    @Query("SELECT showCompletedTracks FROM user_settings WHERE id = 1")
    LiveData<Boolean> getShowCompletedTracks();

    @Query("SELECT showLockedMilestones FROM user_settings WHERE id = 1")
    LiveData<Boolean> getShowLockedMilestones();

    @Query("SELECT stepLengthInMeters FROM user_settings WHERE id = 1")
    LiveData<Float> getStepLength();

    @Query("SELECT stepSource FROM user_settings WHERE id = 1")
    StepSource getStepSource();

    @Query("SELECT refreshTimeInMinutesFitbit FROM user_settings WHERE id = 1")
    LiveData<Integer> getRefreshTimeInMinutesFitbitLive();

    @Query("SELECT refreshTimeInMinutesFitbit FROM user_settings WHERE id = 1")
    Integer getRefreshTimeInMinutesFitbit();
}