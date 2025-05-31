package de.hd.fitbittracks.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.pojos.MilestoneWithStatus;

public class MilestoneRepository {
    private final MilestoneDao milestoneDao;

    public MilestoneRepository(MilestoneDao milestoneDao) {
        this.milestoneDao = milestoneDao;
    }

    public LiveData<List<MilestoneWithStatus>> getAllMilestonesByTrack(long trackId, int stepsWalked) {
        return Transformations.map(
                milestoneDao.getMilestonesForTrackLive(trackId),
                milestones -> milestones.stream().map(milestone -> {
                    MilestoneWithStatus milestoneWithStatus = new MilestoneWithStatus();
                    milestoneWithStatus.milestone = milestone;
                    milestoneWithStatus.isCompleted = stepsWalked >= milestone.stepOffset;
                    return milestoneWithStatus;
                }).collect(Collectors.toList())
        );
    }
}
