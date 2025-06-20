package de.hd.fitbittracks.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.daos.TrackDao;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.pojos.TrackWithMilestones;

public class TrackRepository extends BaseRepository {
    private final TrackDao trackDao;
    private final MilestoneDao milestoneDao;
    public TrackRepository(TrackDao trackDao, MilestoneDao milestoneDao) {
        this.trackDao = trackDao;
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

    public LiveData<List<TrackWithMilestones>> getSortedTracksWithMilestones() {
        return Transformations.map(
                trackDao.getAllTracksWithMilestones(),
                trackWithMilestones -> {
                    if (trackWithMilestones == null) return Collections.emptyList();

                    List<TrackWithMilestones> sortedList = new ArrayList<>(trackWithMilestones);
                    sortedList.sort(new Comparator<>() {
                        @Override
                        public int compare(TrackWithMilestones o1, TrackWithMilestones o2) {
                            long max1 = getMaxDistanceOffset(o1);
                            long max2 = getMaxDistanceOffset(o2);
                            return Long.compare(max1, max2);
                        }

                        private long getMaxDistanceOffset(TrackWithMilestones track) {
                            long max = 0;
                            for (Milestone m : track.milestones) {
                                if (m.distanceOffset > max) {
                                    max = m.distanceOffset;
                                }
                            }
                            return max;
                        }
                    });

                    return sortedList;
                }
        );
    }
}