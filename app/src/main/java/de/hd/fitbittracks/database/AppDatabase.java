package de.hd.fitbittracks.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import java.util.concurrent.Executors;

import de.hd.fitbittracks.converter.Converters;
import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.daos.TrackDao;
import de.hd.fitbittracks.daos.UserProgressDao;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.enums.ProgressStatus;

@Database(entities = {Track.class, Milestone.class, UserProgress.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TrackDao trackDao();
    public abstract MilestoneDao milestoneDao();
    public abstract UserProgressDao userProgressDao();

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

        long trackId = insertMockTrack(db, "Barcelona to Paris", "Barcelona", "Paris", 50000, "barcelona");

        UserProgress progress = new UserProgress();
        progress.trackId = trackId;
        progress.status = ProgressStatus.ACTIVE;
        progress.stepsWalked = 5141;
        db.userProgressDao().upsertProgress(progress);

        // Insert a second track with no progress
        long trackId2 = insertMockTrack(db, "Paris to Berlin", "Paris", "Berlin", 60000, "paris");

        UserProgress progress2 = new UserProgress();
        progress2.trackId = trackId2;
        progress2.status = ProgressStatus.PAUSED;
        progress2.stepsWalked = 1542;
        db.userProgressDao().upsertProgress(progress2);

        // Insert a third track with no progress
        long trackId3 = insertMockTrack(db, "Berlin to Munich", "Berlin", "Munich", 30000, "berlin");
        UserProgress progress3 = new UserProgress();
        progress3.trackId = trackId3;
        progress3.status = ProgressStatus.PAUSED;
        progress3.stepsWalked = 25425;
        db.userProgressDao().upsertProgress(progress3);

        // Insert a fourth track with no progress
        long trackId4 = insertMockTrack(db, "Munich to Zurich", "Munich", "Zurich", 40000, "munich");
        UserProgress progress4 = new UserProgress();
        progress4.trackId = trackId4;
        progress4.status = ProgressStatus.COMPLETED;
        progress4.stepsWalked = 40000; // Assume the user completed this track
        db.userProgressDao().upsertProgress(progress4);
    }

    private static long insertMockTrack(AppDatabase db, String name, String startLocation, String endLocation, int totalSteps, String image) {
        Track track = new Track();
        track.name = name;
        track.startLocation = startLocation;
        track.endLocation = endLocation;
        track.totalSteps = totalSteps;
        track.image = image;

        long trackId = db.trackDao().insertTrack(track);

        insertMockMilestone(db, trackId, 3000, "Milestone 1", "Description for milestone 1", "berlin");
        insertMockMilestone(db, trackId, 15000, "Milestone 2", "Description for milestone 2", "munich");
        insertMockMilestone(db, trackId, 25000, "Milestone 3", "Description for milestone 3", "paris");

        return trackId;
    }

    private static void insertMockMilestone(AppDatabase db, long trackId, int stepOffset, String title, String description, String imageUrl) {
        Milestone milestone = new Milestone();
        milestone.trackId = trackId;
        milestone.stepOffset = stepOffset;
        milestone.title = title;
        milestone.description = description;
        milestone.image = imageUrl;

        db.milestoneDao().insertMilestone(milestone);
    }
}

