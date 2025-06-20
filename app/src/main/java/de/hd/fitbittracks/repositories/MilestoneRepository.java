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

import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.daos.UserProgressDao;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.UserProgressMilestoneStatus;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;

public class MilestoneRepository extends BaseRepository {
    private final MilestoneDao milestoneDao;
    private final UserProgressDao userProgressDao;

    public MilestoneRepository(MilestoneDao milestoneDao, UserProgressDao userProgressDao) {
        this.milestoneDao = milestoneDao;
        this.userProgressDao = userProgressDao;
    }

    public LiveData<List<MilestoneWithStatus>> getAllMilestonesByTrack(long trackId, float distanceWalked, int stepsWalked, long progressId) {
        return Transformations.map(
                milestoneDao.getMilestonesForTrackLive(trackId),
                milestones -> milestones.stream().map(milestone -> {
                    UserProgressMilestoneStatus milestoneStatusForProgress = userProgressDao.getMilestoneStatusForProgress(progressId, milestone.id);
                    MilestoneWithStatus milestoneWithStatus = new MilestoneWithStatus();
                    milestoneWithStatus.milestone = milestone;
                    milestoneWithStatus.isCompleted = distanceWalked >= milestone.distanceOffset;
                    milestoneWithStatus.distanceWalked = distanceWalked > milestone.distanceOffset ? milestone.distanceOffset : distanceWalked;
                    milestoneWithStatus.stepsWalked = milestoneStatusForProgress != null && distanceWalked > milestone.distanceOffset ? milestoneStatusForProgress.stepsWalked : stepsWalked;
                    return milestoneWithStatus;
                }).collect(Collectors.toList())
        );
    }

    public LiveData<List<MilestoneWithStatus>> getMilestonesWithStatus(
            long trackId, long progressId, float distanceWalked, int stepsWalked) {

        MediatorLiveData<List<MilestoneWithStatus>> result = new MediatorLiveData<>();

        LiveData<List<Milestone>> milestonesLive = milestoneDao.getMilestonesForTrackLive(trackId);
        LiveData<List<UserProgressMilestoneStatus>> progressStatusesLive = userProgressDao.getMilestoneStatusesForProgress(progressId);

        result.addSource(milestonesLive, milestones -> {
            List<UserProgressMilestoneStatus> cachedStatuses = progressStatusesLive.getValue();
            if (cachedStatuses != null) {
                result.setValue(combine(milestones, cachedStatuses, distanceWalked, stepsWalked));
            }
        });

        result.addSource(progressStatusesLive, statuses -> {
            List<Milestone> cachedMilestones = milestonesLive.getValue();
            if (cachedMilestones != null) {
                result.setValue(combine(cachedMilestones, statuses, distanceWalked, stepsWalked));
            }
        });

        return result;
    }

    private List<MilestoneWithStatus> combine(List<Milestone> milestones,
                                              List<UserProgressMilestoneStatus> statuses,
                                              float distanceWalked,
                                              int stepsWalked) {

        Map<Long, UserProgressMilestoneStatus> statusMap = new HashMap<>();
        for (UserProgressMilestoneStatus status : statuses) {
            statusMap.put(status.milestoneId, status);
        }

        List<MilestoneWithStatus> result = new ArrayList<>();
        for (Milestone milestone : milestones) {
            MilestoneWithStatus mws = new MilestoneWithStatus();
            mws.milestone = milestone;
            mws.isCompleted = distanceWalked >= milestone.distanceOffset;
            mws.distanceWalked = Math.min(distanceWalked, milestone.distanceOffset);
            UserProgressMilestoneStatus status = statusMap.get(milestone.id);
            mws.stepsWalked = (status != null && distanceWalked > milestone.distanceOffset)
                    ? status.stepsWalked : (distanceWalked > milestone.distanceOffset ? -1 : stepsWalked);
            result.add(mws);
        }

        return result;
    }
}
