package de.hd.stepwise.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.daos.MilestoneDao;
import de.hd.stepwise.daos.UserProgressDao;
import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.ReachedMilestone;
import de.hd.stepwise.pojos.MilestoneImage;
import de.hd.stepwise.pojos.MilestoneExperience;
import de.hd.stepwise.pojos.MilestoneWithStatus;

@Singleton
public class MilestoneRepository extends BaseRepository {
    private final MilestoneDao milestoneDao;
    private final UserProgressDao userProgressDao;

    @Inject
    public MilestoneRepository(AppDatabase db) {
        this.milestoneDao = db.milestoneDao();
        this.userProgressDao = db.userProgressDao();
    }

    public LiveData<List<MilestoneWithStatus>> getMilestonesWithStatus(
            long trackId, long progressId, float distanceWalked, int stepsWalked) {

        MediatorLiveData<List<MilestoneWithStatus>> result = new MediatorLiveData<>();

        LiveData<List<MilestoneWithTotalDistance>> milestonesLive = milestoneDao.getMilestonesForTrackLive(trackId);
        LiveData<List<ReachedMilestone>> reachedMilestonesLive = userProgressDao.observeReachedMilestonesForProgress(progressId);

        result.addSource(milestonesLive, milestones -> {
            List<ReachedMilestone> cachedReachedMilestones = reachedMilestonesLive.getValue();
            if (cachedReachedMilestones != null) {
                result.setValue(combine(milestones, cachedReachedMilestones, distanceWalked, stepsWalked));
            }
        });

        result.addSource(reachedMilestonesLive, reachedMilestones -> {
            List<MilestoneWithTotalDistance> cachedMilestones = milestonesLive.getValue();
            if (cachedMilestones != null) {
                result.setValue(combine(cachedMilestones, reachedMilestones, distanceWalked, stepsWalked));
            }
        });

        return result;
    }

    public LiveData<MilestoneExperience> observeExperience(long progressId, long milestoneId) {
        MediatorLiveData<MilestoneExperience> result = new MediatorLiveData<>();
        LiveData<MilestoneWithTotalDistance> milestone =
                milestoneDao.getMilestoneByIdLive(milestoneId);
        LiveData<ReachedMilestone> reached =
                userProgressDao.observeReachedMilestone(progressId, milestoneId);
        result.addSource(milestone, value ->
                result.setValue(new MilestoneExperience(value, reached.getValue())));
        result.addSource(reached, value ->
                result.setValue(new MilestoneExperience(milestone.getValue(), value)));
        return result;
    }

    public void answerQuiz(long progressId, long milestoneId, int selectedAnswer,
                           boolean correct) {
        executor.execute(() -> userProgressDao.updateReachedMilestoneQuizState(
                progressId, milestoneId, selectedAnswer,
                correct ? System.currentTimeMillis() : null));
    }

    private List<MilestoneWithStatus> combine(List<MilestoneWithTotalDistance> milestones,
                                              List<ReachedMilestone> reachedMilestones,
                                              float distanceWalked,
                                              int stepsWalked) {

        Map<Long, ReachedMilestone> reachedMilestonesById = new HashMap<>();
        for (ReachedMilestone reachedMilestone : reachedMilestones) {
            reachedMilestonesById.put(reachedMilestone.milestoneId, reachedMilestone);
        }

        List<MilestoneWithStatus> result = new ArrayList<>();
        for (MilestoneWithTotalDistance milestone : milestones) {
            MilestoneWithStatus mws = new MilestoneWithStatus();
            mws.milestone = milestone;
            mws.isCompleted = distanceWalked >= milestone.totalDistance;
            mws.distanceWalked = Math.min(distanceWalked, milestone.totalDistance);
            ReachedMilestone reachedMilestone = reachedMilestonesById.get(milestone.id);
            mws.stepsWalked = (reachedMilestone != null && distanceWalked > milestone.totalDistance)
                    ? reachedMilestone.stepsWalked : (distanceWalked > milestone.totalDistance ? -1 : stepsWalked);
            result.add(mws);
        }

        return result;
    }

    public void updateMilestoneImagePath(long milestoneId, String localImagePath) {
        milestoneDao.updateLocalImagePath(milestoneId, localImagePath);
    }

    public void updateMilestoneDetailImagePath(long milestoneId, long position, String path) {
        MilestoneWithTotalDistance milestoneById = milestoneDao.getMilestoneById(milestoneId);
        List<MilestoneImage> images = new ArrayList<>(milestoneById.extraImages);
        images.stream().filter(image -> image.position == position).findFirst().ifPresent(image -> image.localImagePath = path);
        milestoneDao.updateExtraImages(milestoneId, images);
    }
}
