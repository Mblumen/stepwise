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
import de.hd.stepwise.entities.UserProgressMilestoneStatus;
import de.hd.stepwise.pojos.MilestoneImage;
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
        LiveData<List<UserProgressMilestoneStatus>> progressStatusesLive = userProgressDao.getMilestoneStatusesForProgress(progressId);

        result.addSource(milestonesLive, milestones -> {
            List<UserProgressMilestoneStatus> cachedStatuses = progressStatusesLive.getValue();
            if (cachedStatuses != null) {
                result.setValue(combine(milestones, cachedStatuses, distanceWalked, stepsWalked));
            }
        });

        result.addSource(progressStatusesLive, statuses -> {
            List<MilestoneWithTotalDistance> cachedMilestones = milestonesLive.getValue();
            if (cachedMilestones != null) {
                result.setValue(combine(cachedMilestones, statuses, distanceWalked, stepsWalked));
            }
        });

        return result;
    }

    private List<MilestoneWithStatus> combine(List<MilestoneWithTotalDistance> milestones,
                                              List<UserProgressMilestoneStatus> statuses,
                                              float distanceWalked,
                                              int stepsWalked) {

        Map<Long, UserProgressMilestoneStatus> statusMap = new HashMap<>();
        for (UserProgressMilestoneStatus status : statuses) {
            statusMap.put(status.milestoneId, status);
        }

        List<MilestoneWithStatus> result = new ArrayList<>();
        for (MilestoneWithTotalDistance milestone : milestones) {
            MilestoneWithStatus mws = new MilestoneWithStatus();
            mws.milestone = milestone;
            mws.isCompleted = distanceWalked >= milestone.totalDistance;
            mws.distanceWalked = Math.min(distanceWalked, milestone.totalDistance);
            UserProgressMilestoneStatus status = statusMap.get(milestone.id);
            mws.stepsWalked = (status != null && distanceWalked > milestone.totalDistance)
                    ? status.stepsWalked : (distanceWalked > milestone.totalDistance ? -1 : stepsWalked);
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
