package de.hd.stepwise.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.Achievement;
import de.hd.stepwise.entities.Milestone;
import de.hd.stepwise.entities.StepEvent;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.entities.UserProgress;
import de.hd.stepwise.entities.ReachedMilestone;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.enums.AchievementDifficulty;
import de.hd.stepwise.enums.AchievementType;
import de.hd.stepwise.enums.ProgressStatus;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.pojos.events.StepUpdateResult;
import de.hd.stepwise.pojos.events.FinishProgressResult;

@RunWith(AndroidJUnit4.class)
public class UserProgressRepositoryAchievementReconciliationTest {

    private AppDatabase database;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        UserSettings settings = new UserSettings();
        settings.stepLengthInMeters = 1f;
        database.userSettingsDao().insertOrUpdate(settings);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void stepSyncRepairsMissedMilestoneAchievementFromDistinctHistory() {
        Track track = new Track();
        track.name = "Test track";
        track.startLocation = "Start";
        track.endLocation = "End";
        long trackId = database.trackDao().insertTrack(track);

        UserProgress activeProgress = progress(1, trackId, ProgressStatus.ACTIVE);
        UserProgress previousProgress = progress(2, trackId, ProgressStatus.COMPLETED);
        database.userProgressDao().insertUserProgress(activeProgress);
        database.userProgressDao().insertUserProgress(previousProgress);

        database.userProgressDao().insertReachedMilestone(
                new ReachedMilestone(activeProgress.id, 10, 100)
        );
        database.userProgressDao().insertReachedMilestone(
                new ReachedMilestone(previousProgress.id, 10, 100)
        );
        database.userProgressDao().insertReachedMilestone(
                new ReachedMilestone(previousProgress.id, 11, 200)
        );

        database.achievementDao().insert(achievement(
                "REACH_2_MILESTONES",
                AchievementType.MILESTONES_REACHED,
                2
        ));

        new UserProgressRepository(database).updateStepsWalked(0);

        Achievement repaired = database.achievementDao().getByKey("REACH_2_MILESTONES");
        assertEquals(2f, repaired.progressValue, 0f);
        assertTrue(repaired.unlocked);
    }

    @Test
    public void reconciliationCountsDistinctCompletedTracksAndNeverRevokesUnlocks() {
        long firstTrackId = insertTrack("First track");
        long secondTrackId = insertTrack("Second track");
        database.userProgressDao().insertUserProgress(progress(1, firstTrackId, ProgressStatus.COMPLETED));
        database.userProgressDao().insertUserProgress(progress(2, firstTrackId, ProgressStatus.COMPLETED));
        database.userProgressDao().insertUserProgress(progress(3, secondTrackId, ProgressStatus.COMPLETED));

        Achievement achievement = achievement(
                "COMPLETE_3_TRACKS",
                AchievementType.TRACKS_COMPLETED,
                3
        );
        achievement.progressValue = 3;
        achievement.unlocked = true;
        achievement.dateUnlocked = 123L;
        database.achievementDao().insert(achievement);

        new AchievementProgressReconciler(database, () -> 999L).reconcileSilently();

        Achievement repaired = database.achievementDao().getByKey("COMPLETE_3_TRACKS");
        assertEquals(2f, repaired.progressValue, 0f);
        assertTrue(repaired.unlocked);
        assertEquals(Long.valueOf(123L), repaired.dateUnlocked);
    }

    @Test
    public void reconciliationRepairsStepsAndDistanceFromCreditedProgressHistoryIdempotently() {
        long firstTrackId = insertTrack("First track");
        long secondTrackId = insertTrack("Second track");
        UserProgress completedProgress = progress(1, firstTrackId, ProgressStatus.COMPLETED);
        completedProgress.stepsWalked = 4_000;
        completedProgress.distanceWalked = 3_000f;
        UserProgress pausedProgress = progress(2, secondTrackId, ProgressStatus.PAUSED);
        pausedProgress.stepsWalked = 2_500;
        pausedProgress.distanceWalked = 1_750f;
        database.userProgressDao().insertUserProgress(completedProgress);
        database.userProgressDao().insertUserProgress(pausedProgress);
        database.achievementDao().insert(achievement(
                "WALK_10000_STEPS",
                AchievementType.STEPS,
                10_000
        ));
        database.achievementDao().insert(achievement(
                "WALK_10KM",
                AchievementType.DISTANCE,
                10_000
        ));

        AchievementProgressReconciler reconciler = new AchievementProgressReconciler(database, () -> 999L);
        reconciler.reconcileSilently();
        reconciler.reconcileSilently();

        Achievement steps = database.achievementDao().getByKey("WALK_10000_STEPS");
        Achievement distance = database.achievementDao().getByKey("WALK_10KM");
        assertEquals(6_500f, steps.progressValue, 0f);
        assertFalse(steps.unlocked);
        assertEquals(4_750f, distance.progressValue, 0f);
        assertFalse(distance.unlocked);
    }

    @Test
    public void sourceSwitchingDoesNotAddUncreditedSourceEventsToAchievementProgress() {
        long trackId = insertTrack("Source-independent track");
        UserProgress creditedProgress = progress(1, trackId, ProgressStatus.PAUSED);
        creditedProgress.stepsWalked = 100;
        creditedProgress.distanceWalked = 75f;
        database.userProgressDao().insertUserProgress(creditedProgress);
        database.achievementDao().insert(achievement("STEPS", AchievementType.STEPS, 1_000));
        database.achievementDao().insert(achievement("DISTANCE", AchievementType.DISTANCE, 1_000));

        database.stepEventDao().insertStepEvent(new StepEvent(5_000, StepSource.STEP_COUNTER, 100));
        AchievementProgressReconciler reconciler = new AchievementProgressReconciler(database, () -> 999L);
        reconciler.reconcileSilently();
        UserSettings settings = database.userSettingsDao().getSettings();
        settings.stepSource = StepSource.FITBIT;
        database.userSettingsDao().insertOrUpdate(settings);
        database.stepEventDao().insertStepEvent(new StepEvent(7_000, StepSource.FITBIT, 200));

        reconciler.reconcileSilently();

        assertEquals(100f, database.achievementDao().getByKey("STEPS").progressValue, 0f);
        assertEquals(75f, database.achievementDao().getByKey("DISTANCE").progressValue, 0f);
    }

    @Test
    public void silentCatalogReconciliationRepairsNewDefinitionsWithoutRetroactiveEvents() {
        long trackId = insertTrack("Historical track");
        UserProgress historicalProgress = progress(1, trackId, ProgressStatus.COMPLETED);
        historicalProgress.stepsWalked = 120;
        historicalProgress.distanceWalked = 90f;
        database.userProgressDao().insertUserProgress(historicalProgress);
        database.achievementDao().insert(achievement("NEW_STEPS", AchievementType.STEPS, 100));
        database.achievementDao().insert(achievement("NEW_DISTANCE", AchievementType.DISTANCE, 80));
        AchievementProgressReconciler reconciler = new AchievementProgressReconciler(database, () -> 999L);

        reconciler.reconcileSilently();

        assertTrue(database.achievementDao().getByKey("NEW_STEPS").unlocked);
        assertTrue(database.achievementDao().getByKey("NEW_DISTANCE").unlocked);
        assertTrue(reconciler.reconcileInteractively().isEmpty());
    }

    @Test
    public void interactiveReconciliationReportsEachUnlockTransitionOnlyOnce() {
        database.userProgressDao().insertReachedMilestone(
                new ReachedMilestone(1, 10, 100)
        );
        database.userProgressDao().insertReachedMilestone(
                new ReachedMilestone(2, 11, 200)
        );
        database.achievementDao().insert(achievement(
                "REACH_2_MILESTONES",
                AchievementType.MILESTONES_REACHED,
                2
        ));

        AchievementProgressReconciler reconciler = new AchievementProgressReconciler(database, () -> 999L);
        List<Achievement> firstResult = reconciler.reconcileInteractively();
        List<Achievement> secondResult = reconciler.reconcileInteractively();

        assertEquals(1, firstResult.size());
        assertEquals("REACH_2_MILESTONES", firstResult.get(0).key);
        assertEquals(Long.valueOf(999L), firstResult.get(0).dateUnlocked);
        assertTrue(secondResult.isEmpty());
    }

    @Test
    public void oneStepUpdateReconcilesAllMilestonesReachedInThatUpdate() {
        long trackId = insertTrack("Milestone track");
        insertMilestone(10, trackId, 10, "First milestone");
        insertMilestone(11, trackId, 10, "Second milestone");
        database.userProgressDao().insertUserProgress(progress(1, trackId, ProgressStatus.ACTIVE));
        database.achievementDao().insert(achievement(
                "REACH_1_MILESTONE",
                AchievementType.MILESTONES_REACHED,
                1
        ));
        database.achievementDao().insert(achievement(
                "REACH_2_MILESTONES",
                AchievementType.MILESTONES_REACHED,
                2
        ));

        StepUpdateResult result = new UserProgressRepository(database).updateStepsWalked(20);

        assertEquals(2, database.userProgressDao().countDistinctReachedMilestones());
        assertEquals(2, result.reachedMilestones.size());
        assertEquals(2, result.unlockedAchievements.size());
    }

    @Test
    public void stepUpdateReportsStepAchievementUnlockOnlyOnce() {
        long trackId = insertTrack("Step track");
        UserProgress activeProgress = progress(1, trackId, ProgressStatus.ACTIVE);
        activeProgress.stepsWalked = 5;
        activeProgress.distanceWalked = 5f;
        database.userProgressDao().insertUserProgress(activeProgress);
        database.achievementDao().insert(achievement(
                "WALK_10_STEPS",
                AchievementType.STEPS,
                10
        ));
        database.achievementDao().insert(achievement(
                "WALK_10_METERS",
                AchievementType.DISTANCE,
                10
        ));
        UserProgressRepository repository = new UserProgressRepository(database);

        StepUpdateResult unlockResult = repository.updateStepsWalked(5);
        StepUpdateResult repeatedResult = repository.updateStepsWalked(0);

        assertEquals(2, unlockResult.unlockedAchievements.size());
        assertTrue(unlockResult.unlockedAchievements.stream()
                .anyMatch(achievement -> achievement.key.equals("WALK_10_STEPS")));
        assertTrue(unlockResult.unlockedAchievements.stream()
                .anyMatch(achievement -> achievement.key.equals("WALK_10_METERS")));
        assertTrue(repeatedResult.unlockedAchievements.isEmpty());
        assertEquals(10f, database.achievementDao().getByKey("WALK_10_STEPS").progressValue, 0f);
        assertEquals(10f, database.achievementDao().getByKey("WALK_10_METERS").progressValue, 0f);
    }

    @Test
    public void silentRepairPreventsLaterRetroactiveUnlockEvent() {
        database.userProgressDao().insertReachedMilestone(
                new ReachedMilestone(1, 10, 100)
        );
        database.achievementDao().insert(achievement(
                "REACH_1_MILESTONE",
                AchievementType.MILESTONES_REACHED,
                1
        ));

        AchievementProgressReconciler reconciler = new AchievementProgressReconciler(database, () -> 999L);
        reconciler.reconcileSilently();

        assertTrue(reconciler.reconcileInteractively().isEmpty());
        assertTrue(database.achievementDao().getByKey("REACH_1_MILESTONE").unlocked);
    }

    @Test
    public void finishingTracksReconcilesDistinctTracksAndReportsOnlyNewUnlocks() throws InterruptedException {
        long firstTrackId = insertTrack("First track");
        long secondTrackId = insertTrack("Second track");
        database.userProgressDao().insertUserProgress(progress(1, firstTrackId, ProgressStatus.COMPLETED));
        database.userProgressDao().insertUserProgress(progress(2, secondTrackId, ProgressStatus.ACTIVE));
        database.achievementDao().insert(achievement(
                "COMPLETE_2_TRACKS",
                AchievementType.TRACKS_COMPLETED,
                2
        ));
        database.achievementDao().insert(achievement(
                "COMPLETE_3_TRACKS",
                AchievementType.TRACKS_COMPLETED,
                3
        ));
        UserProgressRepository repository = new UserProgressRepository(database);

        FinishProgressResult distinctTrackResult = awaitValue(repository.finishProgress(2));

        assertEquals(1, distinctTrackResult.stepUpdateResult.unlockedAchievements.size());
        assertEquals("COMPLETE_2_TRACKS", distinctTrackResult.stepUpdateResult.unlockedAchievements.get(0).key);

        database.userProgressDao().insertUserProgress(progress(3, secondTrackId, ProgressStatus.ACTIVE));
        FinishProgressResult repeatedTrackResult = awaitValue(repository.finishProgress(3));

        assertTrue(repeatedTrackResult.stepUpdateResult.unlockedAchievements.isEmpty());
        Achievement threeTracks = database.achievementDao().getByKey("COMPLETE_3_TRACKS");
        assertEquals(2f, threeTracks.progressValue, 0f);
        assertFalse(threeTracks.unlocked);
    }

    private UserProgress progress(long id, long trackId, ProgressStatus status) {
        UserProgress progress = new UserProgress();
        progress.id = id;
        progress.trackId = trackId;
        progress.status = status;
        return progress;
    }

    private long insertTrack(String name) {
        Track track = new Track();
        track.name = name;
        track.startLocation = "Start";
        track.endLocation = "End";
        return database.trackDao().insertTrack(track);
    }

    private void insertMilestone(long id, long trackId, int offset, String title) {
        Milestone milestone = new Milestone();
        milestone.id = id;
        milestone.trackId = trackId;
        milestone.distanceOffsetToPrevious = offset;
        milestone.title = title;
        milestone.description = title;
        database.milestoneDao().insertMilestone(milestone);
    }

    private Achievement achievement(String key, AchievementType type, float target) {
        Achievement achievement = new Achievement();
        achievement.key = key;
        achievement.title = key;
        achievement.description = key;
        achievement.icon = "map";
        achievement.type = type;
        achievement.difficulty = AchievementDifficulty.BRONZE;
        achievement.targetValue = target;
        return achievement;
    }

    private <T> T awaitValue(LiveData<T> liveData) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> value = new AtomicReference<>();
        Observer<T> observer = result -> {
            value.set(result);
            latch.countDown();
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> liveData.observeForever(observer)
        );
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> liveData.removeObserver(observer)
        );
        assertTrue("LiveData did not emit within five seconds", completed);
        return value.get();
    }
}
