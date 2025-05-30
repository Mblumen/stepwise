package de.hd.fitbittracks.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.entities.Milestone;

public class TrackRepository {

    private final MilestoneDao milestoneDao;

    public TrackRepository(MilestoneDao milestoneDao) {
        this.milestoneDao = milestoneDao;
    }

    public LiveData<Map<Long, List<Milestone>>> getAllMilestonesByTrack() {
        return Transformations.map(
                milestoneDao.getAllMilestones(),
                milestones -> {
                    Map<Long, List<Milestone>> grouped = new HashMap<>();
                    for (Milestone m : milestones) {
                        if (!grouped.containsKey(m.trackId)) {
                            grouped.put(m.trackId, new ArrayList<>());
                        }
                        Objects.requireNonNull(grouped.get(m.trackId)).add(m);
                    }
                    return grouped;
                }
        );
    }
}