package de.hd.stepwise.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.database.Cursor;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseMigrationTest {

    private static final String TEST_DATABASE = "migration-test";

    @Rule
    public final MigrationTestHelper helper = new MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase.class
    );

    @Test
    public void migrationFrom5To6PreservesReachedMilestonesWithoutNotificationState() throws Exception {
        SupportSQLiteDatabase database = helper.createDatabase(TEST_DATABASE, 5);
        database.execSQL(
                "INSERT INTO user_progress_milestone_status "
                        + "(progressId, milestoneId, notified, stepsWalked) VALUES (7, 11, 1, 1234)"
        );
        database.execSQL(
                "INSERT INTO user_progress_milestone_status "
                        + "(progressId, milestoneId, notified, stepsWalked) VALUES (7, 12, 0, 1500)"
        );
        database.close();

        database = helper.runMigrationsAndValidate(
                TEST_DATABASE,
                6,
                true,
                AppDatabase.MIGRATION_5_6
        );

        try (Cursor cursor = database.query(
                "SELECT progressId, milestoneId, stepsWalked FROM reached_milestone"
        )) {
            assertEquals(1, cursor.getCount());
            cursor.moveToFirst();
            assertEquals(7L, cursor.getLong(0));
            assertEquals(11L, cursor.getLong(1));
            assertEquals(1234, cursor.getInt(2));
            assertEquals(3, cursor.getColumnCount());
        }
    }

    @Test
    public void migrationFrom6To7StartsEmptyStreakHistoryOnMigrationDate() throws Exception {
        SupportSQLiteDatabase database = helper.createDatabase(TEST_DATABASE, 6);
        database.execSQL("INSERT INTO user_settings "
                + "(id, stepLengthInMeters, showCompletedTracks, useDarkMode, showLockedMilestones, "
                + "refreshTimeInMinutesFitbit, stepSource) VALUES (1, 0.75, 1, 1, 0, 5, 0)");
        database.execSQL("INSERT INTO daily_steps (date, steps, source, lastUpdated, addedStepsSinceLastUpdate) "
                + "VALUES ('2026-08-01', 12000, 1, 0, 0)");
        database.close();

        database = helper.runMigrationsAndValidate(
                TEST_DATABASE,
                7,
                true,
                AppDatabase.MIGRATION_6_7
        );

        try (Cursor cursor = database.query("SELECT COUNT(*) FROM daily_activity")) {
            cursor.moveToFirst();
            assertEquals(0, cursor.getInt(0));
        }
        try (Cursor cursor = database.query(
                "SELECT streakTrackingStartDate FROM user_settings WHERE id = 1"
        )) {
            cursor.moveToFirst();
            assertNotNull(cursor.getString(0));
        }
    }

    @Test
    public void migrationFrom7To8PreservesJourneyHistoryAndAddsRichContent() throws Exception {
        SupportSQLiteDatabase database = helper.createDatabase(TEST_DATABASE, 7);
        database.execSQL("INSERT INTO track "
                + "(id, name, startLocation, endLocation, challengeDuration) "
                + "VALUES (3, 'Test Track', 'Start', 'End', 0)");
        database.execSQL("INSERT INTO milestone "
                + "(id, trackId, distanceOffsetToPrevious, title, description, latitude, "
                + "longitude, unlocked) VALUES (11, 3, 1000, 'Stop', 'Description', 0, 0, 1)");
        database.execSQL("INSERT INTO user_progress "
                + "(id, trackId, stepsWalked, distanceWalked, status, startedAt, completedAt) "
                + "VALUES (7, 3, 1234, 1000, 'completed', 10000, 20000)");
        database.execSQL("INSERT INTO reached_milestone "
                + "(progressId, milestoneId, stepsWalked) VALUES (7, 11, 1234)");
        database.execSQL("INSERT INTO user_progress "
                + "(id, trackId, stepsWalked, distanceWalked, status, startedAt) "
                + "VALUES (8, 3, 500, 400, 'paused', 30000)");
        database.execSQL("INSERT INTO reached_milestone "
                + "(progressId, milestoneId, stepsWalked) VALUES (8, 11, 500)");
        database.close();

        database = helper.runMigrationsAndValidate(
                TEST_DATABASE,
                8,
                true,
                AppDatabase.MIGRATION_7_8
        );

        try (Cursor cursor = database.query("SELECT reachedAt, selectedQuizAnswer, "
                + "quizCompletedAt FROM reached_milestone WHERE progressId = 7 AND milestoneId = 11")) {
            cursor.moveToFirst();
            assertEquals(20000L, cursor.getLong(0));
            assertEquals(true, cursor.isNull(1));
            assertEquals(true, cursor.isNull(2));
        }
        try (Cursor cursor = database.query("SELECT reachedAt FROM reached_milestone "
                + "WHERE progressId = 8 AND milestoneId = 11")) {
            cursor.moveToFirst();
            assertEquals(30000L, cursor.getLong(0));
        }
        try (Cursor cursor = database.query("SELECT audioUrl, localAudioPath, stampImageUrl, "
                + "localStampImagePath, discovery, quiz FROM milestone WHERE id = 11")) {
            cursor.moveToFirst();
            for (int index = 0; index < cursor.getColumnCount(); index++) {
                assertEquals(true, cursor.isNull(index));
            }
        }
    }

    @Test
    public void migrationFrom8To9KeepsDailyStepsAndStartsANewStreakLedger() throws Exception {
        SupportSQLiteDatabase database = helper.createDatabase(TEST_DATABASE, 8);
        database.execSQL("INSERT INTO user_settings "
                + "(id, stepLengthInMeters, showCompletedTracks, useDarkMode, showLockedMilestones, "
                + "refreshTimeInMinutesFitbit, stepSource, streakTrackingStartDate) "
                + "VALUES (1, 0.75, 1, 1, 0, 5, 0, '2026-01-01')");
        database.execSQL("INSERT INTO daily_activity "
                + "(date, sensorSteps, fitbitSteps, fitbitLastObserved) "
                + "VALUES ('2026-08-29', 3000, 2500, NULL)");
        database.close();

        database = helper.runMigrationsAndValidate(
                TEST_DATABASE,
                9,
                true,
                AppDatabase.MIGRATION_8_9
        );

        try (Cursor cursor = database.query("SELECT sensorSteps + fitbitSteps FROM daily_activity")) {
            cursor.moveToFirst();
            assertEquals(5_500, cursor.getInt(0));
        }
        try (Cursor cursor = database.query("SELECT steps, effectiveDate FROM daily_step_goal")) {
            cursor.moveToFirst();
            assertEquals(5_000, cursor.getInt(0));
            assertNotNull(cursor.getString(1));
        }
        try (Cursor cursor = database.query("SELECT COUNT(*) FROM daily_streak_outcome")) {
            cursor.moveToFirst();
            assertEquals(0, cursor.getInt(0));
        }
    }
}
