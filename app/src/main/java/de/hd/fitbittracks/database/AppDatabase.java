package de.hd.fitbittracks.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import de.hd.fitbittracks.converter.Converters;
import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.daos.TrackDao;
import de.hd.fitbittracks.daos.UserProgressDao;
import de.hd.fitbittracks.daos.UserSettingsDao;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.entities.UserProgressMilestoneStatus;
import de.hd.fitbittracks.entities.UserSettings;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.MilestoneImage;

@Database(entities = {Track.class, Milestone.class, UserProgress.class, UserProgressMilestoneStatus.class, UserSettings.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TrackDao trackDao();
    public abstract MilestoneDao milestoneDao();
    public abstract UserProgressDao userProgressDao();

    public abstract UserSettingsDao userSettingsDao();

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
        userSettings.stepLengthInMeters = 0.8f; // Default step length
        userSettings.useDarkMode = false;
        db.userSettingsDao().insertOrUpdate(userSettings);

        long trackId = insertMockTrack(db, "Barcelona to Paris", "Barcelona", "Paris", 50000, "barcelona", true);

        UserProgress progress = new UserProgress();
        progress.trackId = trackId;
        progress.status = ProgressStatus.ACTIVE;
        progress.stepsWalked = 2995;
        progress.distanceWalked = 2995;
        db.userProgressDao().insertUserProgress(progress);

        // Insert a second track with no progress
        long trackId2 = insertMockTrack(db, "Paris to Berlin", "Paris", "Berlin", 60000, "paris", true);

        UserProgress progress2 = new UserProgress();
        progress2.trackId = trackId2;
        progress2.status = ProgressStatus.PAUSED;
        progress2.stepsWalked = 1542;
        progress2.distanceWalked = 1542;;
        db.userProgressDao().insertUserProgress(progress2);

        // Insert a third track with no progress
        long trackId3 = insertMockTrack(db, "Berlin to Munich", "Berlin", "Munich", 30000, "berlin", true);
        UserProgress progress3 = new UserProgress();
        progress3.trackId = trackId3;
        progress3.status = ProgressStatus.PAUSED;
        progress3.stepsWalked = 25425;
        progress3.distanceWalked = 25425; // Assume the user has walked 25425 steps on this track
        db.userProgressDao().insertUserProgress(progress3);

        // Insert a fourth track with no progress
        long trackId4 = insertMockTrack(db, "Munich to Zurich", "Munich", "Zurich", 9000, "munich", false);
        insertMockMilestone(db, trackId4, 3000, "Milestone 1", "Description for milestone 1", "berlin", Collections.emptyList(), "", 47.3768866, 8.541694);
        UserProgress progress4 = new UserProgress();
        progress4.trackId = trackId4;
        progress4.status = ProgressStatus.COMPLETED;
        progress4.distanceWalked = 9000; // Assume the user has walked 9000 steps on this track;
        progress4.stepsWalked = 9000; // Assume the user completed this track
        db.userProgressDao().insertUserProgress(progress4);
    }

    private static long insertMockTrack(AppDatabase db, String name, String startLocation, String endLocation, int totalSteps, String image, boolean addMilestones) {
        Track track = new Track();
        track.name = name;
        track.startLocation = startLocation;
        track.endLocation = endLocation;
        track.totalDistance = totalSteps;
        track.totalSteps = totalSteps;
        track.image = image;

        long trackId = db.trackDao().insertTrack(track);

        if(!addMilestones) return trackId;

        insertMockMilestone(db, trackId, 3000, "Milestone 1", "Description for milestone 1. Diese ist länger als sie in die Zeile passt, mal schauen, was passiert.", "berlin", List.of(new MilestoneImage("munich", "Eine Beschreibung zu München"), new MilestoneImage("paris", "Der Eifelturm"), new  MilestoneImage("berlin", "Brandenburger Tor")), "https://www.google.de/maps/place/Berliner+Fernsehturm/@52.5208182,13.4068442,17z/data=!3m1!5s0x47a84e1f8930e30b:0x45589dc39d6724c4!4m6!3m5!1s0x47a84e1f9014ffeb:0xc8fafc484349e4a1!8m2!3d52.520815!4d13.4094191!16zL20vMDJnOG4w?entry=ttu&g_ep=EgoyMDI1MDUyOC4wIKXMDSoASAFQAw%3D%3D", 0, 0);
        insertMockMilestone(db, trackId, 15000, "Milestone 2", "Description for milestone 2", "munich", Collections.emptyList(), "", 49.38440050589991, 8.678341220795584);
        insertMockMilestone(db, trackId, 25000, "Milestone 3", "Description for milestone 3", "paris", Collections.emptyList(), "", 0, 0);

        return trackId;
    }

    private static void insertMockMilestone(AppDatabase db, long trackId, int stepOffset, String title, String description, String imageUrl, List<MilestoneImage> extraImages, String mapsUrl, double lat, double lon) {
        Milestone milestone = new Milestone();
        milestone.trackId = trackId;
        milestone.stepOffset = stepOffset;
        milestone.distanceOffset = stepOffset;
        milestone.title = title;
        milestone.description = description;
        milestone.image = imageUrl;
        milestone.extraImages = extraImages != null ? extraImages : new ArrayList<>();
        milestone.mapsUrl = mapsUrl;
        milestone.latitude = lat;
        milestone.longitude = lon;

        db.milestoneDao().insertMilestone(milestone);
    }
}

