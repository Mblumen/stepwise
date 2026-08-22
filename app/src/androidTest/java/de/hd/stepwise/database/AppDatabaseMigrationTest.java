package de.hd.stepwise.database;

import static org.junit.Assert.assertEquals;

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
}
