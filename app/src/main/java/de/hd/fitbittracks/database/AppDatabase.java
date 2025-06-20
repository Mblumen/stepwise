package de.hd.fitbittracks.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import de.hd.fitbittracks.converter.Converters;
import de.hd.fitbittracks.daos.AchievementDao;
import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.daos.TrackDao;
import de.hd.fitbittracks.daos.UserProgressDao;
import de.hd.fitbittracks.daos.UserSettingsDao;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.entities.UserProgressMilestoneStatus;
import de.hd.fitbittracks.entities.UserSettings;
import de.hd.fitbittracks.enums.AchievementDifficulty;
import de.hd.fitbittracks.enums.AchievementType;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.MilestoneImage;

@Database(entities = {Track.class, Milestone.class, UserProgress.class, UserProgressMilestoneStatus.class, UserSettings.class, Achievement.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TrackDao trackDao();
    public abstract MilestoneDao milestoneDao();
    public abstract UserProgressDao userProgressDao();

    public abstract UserSettingsDao userSettingsDao();

    public abstract AchievementDao achievementDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fitbit_tracks_db"
                            )
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        insertMockData(getInstance(context));
                                    });
                                }
                            })
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void insertMockData(AppDatabase db) {
        // Insert a mock user settings
        UserSettings userSettings = new UserSettings();
        userSettings.showCompletedTracks = true;
//        userSettings.showPausedTracks = true;
//        userSettings.showActiveTracks = true;
//        userSettings.showMilestones = true;
//        userSettings.showTrackImages = true;
//        userSettings.showTrackDescriptions = true;
//        userSettings.showTrackDistances = true;
//        userSettings.showTrackStartEndLocations = true;
//        userSettings.showTrackSteps = true;
//        userSettings.showTrackProgress = true;
//        userSettings.showTrackStatus = true;
//        userSettings.showTrackMaps = true;
//        userSettings.showTrackStartEndLocations = true;
//        userSettings.showTrackMilestones = true;
//        userSettings.showTrackMilestoneImages = true;
//        userSettings.showTrackMilestoneDescriptions = true;
//        userSettings.showTrackMilestoneMaps = true;
        userSettings.stepLengthInMeters = 1f; // Default step length
        userSettings.useDarkMode = true;
        db.userSettingsDao().insertOrUpdate(userSettings);

        long trackId = insertMockTrack(db, "Barcelona to Paris", "Barcelona", "Paris", "barcelona");
        insertMockMilestone(db, trackId, 3000, "Milestone 1", "Description for milestone 1. Diese ist länger als sie in die Zeile passt, mal schauen, was passiert.", "berlin", List.of(new MilestoneImage("munich", "Eine Beschreibung zu München"), new MilestoneImage("paris", "Der Eifelturm"), new  MilestoneImage("berlin", "Brandenburger Tor")), "https://www.google.de/maps/place/Berliner+Fernsehturm/@52.5208182,13.4068442,17z/data=!3m1!5s0x47a84e1f8930e30b:0x45589dc39d6724c4!4m6!3m5!1s0x47a84e1f9014ffeb:0xc8fafc484349e4a1!8m2!3d52.520815!4d13.4094191!16zL20vMDJnOG4w?entry=ttu&g_ep=EgoyMDI1MDUyOC4wIKXMDSoASAFQAw%3D%3D", 0, 0, false);
        insertMockMilestone(db, trackId, 15000, "Milestone 2", "Description for milestone 2", "munich", Collections.emptyList(), "", 49.38440050589991, 8.678341220795584, false);
        insertMockMilestone(db, trackId, 25000, "Milestone 3", "Description for milestone 3", "paris", Collections.emptyList(), "", 0, 0, false);
        UserProgress progress = new UserProgress();
        progress.trackId = trackId;
        progress.status = ProgressStatus.PAUSED;
        progress.stepsWalked = 2995;
        progress.distanceWalked = 2995;
        long progressId1 = db.userProgressDao().insertUserProgress(progress);

        long trackId2 = insertMockTrack(db, "Paris to Berlin", "Paris", "Berlin", "paris");
        insertMockMilestone(db, trackId2, 3000, "Milestone 1", "Description for milestone 1. Diese ist länger als sie in die Zeile passt, mal schauen, was passiert.", "berlin", List.of(new MilestoneImage("munich", "Eine Beschreibung zu München"), new MilestoneImage("paris", "Der Eifelturm"), new  MilestoneImage("berlin", "Brandenburger Tor")), "https://www.google.de/maps/place/Berliner+Fernsehturm/@52.5208182,13.4068442,17z/data=!3m1!5s0x47a84e1f8930e30b:0x45589dc39d6724c4!4m6!3m5!1s0x47a84e1f9014ffeb:0xc8fafc484349e4a1!8m2!3d52.520815!4d13.4094191!16zL20vMDJnOG4w?entry=ttu&g_ep=EgoyMDI1MDUyOC4wIKXMDSoASAFQAw%3D%3D", 0, 0, false);
        insertMockMilestone(db, trackId2, 15000, "Milestone 2", "Description for milestone 2", "munich", Collections.emptyList(), "", 49.38440050589991, 8.678341220795584, false);
        insertMockMilestone(db, trackId2, 25000, "Milestone 3", "Description for milestone 3", "paris", Collections.emptyList(), "", 0, 0, false);
        UserProgress progress2 = new UserProgress();
        progress2.trackId = trackId2;
        progress2.status = ProgressStatus.ACTIVE;
        progress2.stepsWalked = 1542;
        progress2.distanceWalked = 1542;;
        long progressId2 = db.userProgressDao().insertUserProgress(progress2);

        long trackId3 = insertMockTrack(db, "Berlin to Munich", "Berlin", "Munich", "berlin");
        long trackId3MilestoneId1 = insertMockMilestone(db, trackId3, 3000, "Milestone 1", "Description for milestone 1. Diese ist länger als sie in die Zeile passt, mal schauen, was passiert.", "berlin", List.of(new MilestoneImage("munich", "Eine Beschreibung zu München"), new MilestoneImage("paris", "Der Eifelturm"), new  MilestoneImage("berlin", "Brandenburger Tor")), "https://www.google.de/maps/place/Berliner+Fernsehturm/@52.5208182,13.4068442,17z/data=!3m1!5s0x47a84e1f8930e30b:0x45589dc39d6724c4!4m6!3m5!1s0x47a84e1f9014ffeb:0xc8fafc484349e4a1!8m2!3d52.520815!4d13.4094191!16zL20vMDJnOG4w?entry=ttu&g_ep=EgoyMDI1MDUyOC4wIKXMDSoASAFQAw%3D%3D", 0, 0, true);
        long trackId3MilestoneId2 = insertMockMilestone(db, trackId3, 15000, "Milestone 2", "Description for milestone 2", "munich", Collections.emptyList(), "", 49.38440050589991, 8.678341220795584, true);
        long trackId3MilestoneId3 = insertMockMilestone(db, trackId3, 25000, "Milestone 3", "Description for milestone 3", "paris", Collections.emptyList(), "", 0, 0, true);
        UserProgress progress3 = new UserProgress();
        progress3.trackId = trackId3;
        progress3.status = ProgressStatus.PAUSED;
        progress3.stepsWalked = 26731;
        progress3.distanceWalked = 24995; // Assume the user has walked 25425 steps on this track
        long progressId3 = db.userProgressDao().insertUserProgress(progress3);
        // Insert a UserProgressMilestoneStatus for the first milestone
        UserProgressMilestoneStatus milestoneStatus1 = new UserProgressMilestoneStatus(progressId3, trackId3MilestoneId1, true, 3152);
        db.userProgressDao().markMilestoneNotified(milestoneStatus1);
        // Insert a UserProgressMilestoneStatus for the second milestone
        UserProgressMilestoneStatus milestoneStatus2 = new UserProgressMilestoneStatus(progressId3, trackId3MilestoneId2, true, 16330);
        db.userProgressDao().markMilestoneNotified(milestoneStatus2);
        // Insert a UserProgressMilestoneStatus for the third milestone
        UserProgressMilestoneStatus milestoneStatus3 = new UserProgressMilestoneStatus(progressId3, trackId3MilestoneId3, true, 27532);
        //db.userProgressDao().markMilestoneNotified(milestoneStatus3);

        long trackId4 = insertMockTrack(db, "Munich to Zurich", "Munich", "Zurich", "munich");
        long trackId4MilestoneId1 = insertMockMilestone(db, trackId4, 3000, "Milestone 1", "Description for milestone 1", "berlin", Collections.emptyList(), "", 47.3768866, 8.541694, true);
        long trackId4MilestoneId2 = insertMockMilestone(db, trackId4, 9000, "Milestone 2", "Description for milestone 2", "paris", Collections.emptyList(), "", 0, 0, true);
        UserProgress progress4 = new UserProgress();
        progress4.trackId = trackId4;
        progress4.status = ProgressStatus.COMPLETED;
        progress4.distanceWalked = 9000; // Assume the user has walked 9000 steps on this track;
        progress4.stepsWalked = 9621; // Assume the user completed this track
        long progressId4 = db.userProgressDao().insertUserProgress(progress4);
        // Insert a UserProgressMilestoneStatus for the first milestone
        UserProgressMilestoneStatus milestoneStatus4 = new UserProgressMilestoneStatus(progressId4, trackId4MilestoneId1, true, 3251);
        db.userProgressDao().markMilestoneNotified(milestoneStatus4);
        // Insert a UserProgressMilestoneStatus for the second milestone
        UserProgressMilestoneStatus milestoneStatus5 = new UserProgressMilestoneStatus(progressId4, trackId4MilestoneId2, true, 9621);
        db.userProgressDao().markMilestoneNotified(milestoneStatus5);

        addAchievements(db);
    }

    private static long insertMockTrack(AppDatabase db, String name, String startLocation, String endLocation, String image) {
        Track track = new Track();
        track.name = name;
        track.startLocation = startLocation;
        track.endLocation = endLocation;
        track.image = image;

        return db.trackDao().insertTrack(track);
    }

    private static long insertMockMilestone(AppDatabase db, long trackId, int distanceOffset, String title, String description, String imageUrl, List<MilestoneImage> extraImages, String mapsUrl, double lat, double lon, boolean unlocked) {
        Milestone milestone = new Milestone();
        milestone.trackId = trackId;
        milestone.distanceOffset = distanceOffset;
        milestone.title = title;
        milestone.description = description;
        milestone.image = imageUrl;
        milestone.extraImages = extraImages != null ? extraImages : new ArrayList<>();
        milestone.mapsUrl = mapsUrl;
        milestone.latitude = lat;
        milestone.longitude = lon;
        milestone.unlocked = unlocked;

        return db.milestoneDao().insertMilestone(milestone);
    }

    private static void addAchievements(AppDatabase db) {
        // This method can be used to add achievements or other data if needed
        Log.d("AppDatabase", "Adding achievements is not implemented yet.");
        Achievement walk10Km = new Achievement(
                "WALK_10KM",
                "Centurion Walker",
                "Walk a total of 100 km",
                "steps",
                AchievementType.DISTANCE,
                AchievementDifficulty.BRONZE,
                10000f,  // in meters
                10000f,
                true,
                System.currentTimeMillis()  // Set the date unlocked to now
        );
        db.achievementDao().insert(walk10Km);
        Achievement walk20Km = new Achievement(
                "WALK_10KM",
                "Centurion Walker",
                "Walk a total of 100 km",
                "steps",
                AchievementType.DISTANCE,
                AchievementDifficulty.SILVER,
                20000f,  // in meters
                19995f,
                false,
                null
        );
        db.achievementDao().insert(walk20Km);
        Achievement walk100Km = new Achievement(
                "WALK_100KM",
                "Centurion Walker",
                "Walk a total of 100 km",
                "steps",
                AchievementType.DISTANCE,
                AchievementDifficulty.GOLD,
                100000f,  // in meters
                19995f,
                false,
                null
        );
        db.achievementDao().insert(walk100Km);

    }
}

