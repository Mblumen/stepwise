package de.hd.fitbittracks.pojos.events;

import android.util.Pair;

import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.entities.UserProgress;

public class MilestoneWithProgressEvent extends MessageEvent<Pair<MilestoneWithTotalDistance, UserProgress>> {
    public MilestoneWithProgressEvent(Pair<MilestoneWithTotalDistance, UserProgress> content, String message) {
        super(content, message);
    }
    public MilestoneWithProgressEvent(Pair<MilestoneWithTotalDistance, UserProgress> content) {
        super(content);
    }
}
