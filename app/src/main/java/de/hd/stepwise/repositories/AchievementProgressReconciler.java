package de.hd.stepwise.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.AchievementDao;
import de.hd.stepwise.daos.UserProgressDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.Achievement;
import de.hd.stepwise.enums.AchievementType;
import de.hd.stepwise.enums.ProgressStatus;

@Singleton
public class AchievementProgressReconciler {

    private final AppDatabase database;
    private final AchievementDao achievementDao;
    private final UserProgressDao userProgressDao;
    private final LongSupplier currentTimeMillis;

    @Inject
    public AchievementProgressReconciler(AppDatabase database) {
        this(database, System::currentTimeMillis);
    }

    AchievementProgressReconciler(AppDatabase database, LongSupplier currentTimeMillis) {
        this.database = database;
        this.achievementDao = database.achievementDao();
        this.userProgressDao = database.userProgressDao();
        this.currentTimeMillis = currentTimeMillis;
    }

    public void reconcileSilently() {
        database.runInTransaction(() -> reconcileInCurrentTransaction(false));
    }

    public List<Achievement> reconcileInteractively() {
        return database.runInTransaction(() -> reconcileInCurrentTransaction(true));
    }

    List<Achievement> reconcileInteractivelyInCurrentTransaction() {
        return reconcileInCurrentTransaction(true);
    }

    private List<Achievement> reconcileInCurrentTransaction(boolean reportNewUnlocks) {
        int distinctMilestones = userProgressDao.countDistinctReachedMilestones();
        int distinctCompletedTracks = userProgressDao.countDistinctTracksWithStatus(ProgressStatus.COMPLETED);
        int totalCreditedSteps = userProgressDao.getTotalCreditedSteps();
        float totalCreditedDistance = userProgressDao.getTotalCreditedDistance();
        List<Achievement> newlyUnlocked = new ArrayList<>();

        List<Achievement> achievements = achievementDao.getAchievementsByType(List.of(
                AchievementType.DISTANCE,
                AchievementType.STEPS,
                AchievementType.MILESTONES_REACHED,
                AchievementType.TRACKS_COMPLETED
        ));
        for (Achievement achievement : achievements) {
            float canonicalValue = switch (achievement.type) {
                case DISTANCE -> totalCreditedDistance;
                case STEPS -> totalCreditedSteps;
                case MILESTONES_REACHED -> distinctMilestones;
                case TRACKS_COMPLETED -> distinctCompletedTracks;
                default -> achievement.progressValue;
            };
            canonicalValue = Math.min(canonicalValue, achievement.targetValue);

            boolean wasUnlocked = achievement.unlocked;
            boolean reachedTarget = canonicalValue >= achievement.targetValue;
            achievement.progressValue = canonicalValue;
            achievement.unlocked = wasUnlocked || reachedTarget;

            if (!wasUnlocked && reachedTarget) {
                achievement.dateUnlocked = currentTimeMillis.getAsLong();
                if (reportNewUnlocks) {
                    newlyUnlocked.add(achievement);
                }
            }

            achievementDao.update(achievement);
        }

        return newlyUnlocked;
    }
}
