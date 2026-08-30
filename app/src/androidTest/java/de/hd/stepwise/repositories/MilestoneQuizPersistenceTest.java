package de.hd.stepwise.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.Milestone;
import de.hd.stepwise.entities.ReachedMilestone;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.entities.UserProgress;
import de.hd.stepwise.enums.ProgressStatus;

@RunWith(AndroidJUnit4.class)
public class MilestoneQuizPersistenceTest {
    private AppDatabase database;

    @Before
    public void setUp() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(), AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        Track track = new Track();
        track.id = 3;
        database.trackDao().insertTrack(track);
        Milestone milestone = new Milestone();
        milestone.id = 11;
        milestone.trackId = 3;
        database.milestoneDao().insertMilestone(milestone);
        UserProgress progress = new UserProgress();
        progress.id = 7;
        progress.trackId = 3;
        progress.status = ProgressStatus.ACTIVE;
        database.userProgressDao().insertUserProgress(progress);
        database.userProgressDao().insertReachedMilestone(
                new ReachedMilestone(7, 11, 100, 1_000));
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void correctCompletionSurvivesReloadWhileIncorrectAnswerIsIncomplete() {
        database.userProgressDao().updateReachedMilestoneQuizState(7, 11, 0, null);
        ReachedMilestone incorrect = database.userProgressDao().getReachedMilestone(7, 11);
        assertEquals(0, incorrect.selectedQuizAnswer.intValue());
        assertNull(incorrect.quizCompletedAt);

        database.userProgressDao().updateReachedMilestoneQuizState(7, 11, 1, 2_000L);
        ReachedMilestone correct = database.userProgressDao().getReachedMilestone(7, 11);
        assertEquals(1, correct.selectedQuizAnswer.intValue());
        assertNotNull(correct.quizCompletedAt);
    }
}
