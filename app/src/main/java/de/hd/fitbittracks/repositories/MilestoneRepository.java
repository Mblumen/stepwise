package de.hd.fitbittracks.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.daos.UserProgressDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.entities.UserProgressMilestoneStatus;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;

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
}
