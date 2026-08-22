package de.hd.stepwise.entities;

import androidx.room.Entity;

@Entity(
        tableName = "reached_milestone",
        primaryKeys = {"progressId", "milestoneId"}
)
public class ReachedMilestone {
    public long progressId;
    public long milestoneId;
    public int stepsWalked;

    public ReachedMilestone(long progressId, long milestoneId, int stepsWalked) {
        this.progressId = progressId;
        this.milestoneId = milestoneId;
        this.stepsWalked = stepsWalked;
    }
}
