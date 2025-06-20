package de.hd.fitbittracks.pojos.events;

import android.util.Pair;

import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.UserProgress;

public class MilestoneWithProgressEvent extends MessageEvent<Pair<Milestone, UserProgress>> {
    public MilestoneWithProgressEvent(Pair<Milestone, UserProgress> content, String message) {
        super(content, message);
    }
    public MilestoneWithProgressEvent(Pair<Milestone, UserProgress> content) {
        super(content);
    }
}
