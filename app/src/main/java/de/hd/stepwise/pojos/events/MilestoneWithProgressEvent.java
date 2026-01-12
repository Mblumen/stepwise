package de.hd.stepwise.pojos.events;

import android.util.Pair;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.UserProgress;

public class MilestoneWithProgressEvent extends MessageEvent<Pair<MilestoneWithTotalDistance, UserProgress>> {
    public MilestoneWithProgressEvent(Pair<MilestoneWithTotalDistance, UserProgress> content, String message) {
        super(content, message);
    }
    public MilestoneWithProgressEvent(Pair<MilestoneWithTotalDistance, UserProgress> content) {
        super(content);
    }
}
