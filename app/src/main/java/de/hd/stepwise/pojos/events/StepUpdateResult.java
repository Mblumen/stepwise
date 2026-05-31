package de.hd.stepwise.pojos.events;

import java.util.ArrayList;
import java.util.List;

import de.hd.stepwise.entities.Achievement;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.entities.UserProgress;

public class StepUpdateResult {
    public List<MilestoneWithTotalDistance> reachedMilestones = new ArrayList<>();
    public List<Achievement> unlockedAchievements = new ArrayList<>();
    public Track finishedTrack = null;
    public UserProgress progress;
}