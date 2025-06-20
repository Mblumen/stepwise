package de.hd.fitbittracks.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "user_progress_milestone_status",
        primaryKeys = {"progressId", "milestoneId"}
)
public class UserProgressMilestoneStatus {
    public long progressId;
    public long milestoneId;
    public boolean notified; // optional if only store notified rows
    public int stepsWalked;

    public UserProgressMilestoneStatus(long progressId, long milestoneId, boolean notified, int stepsWalked) {
        this.progressId = progressId;
        this.milestoneId = milestoneId;
        this.notified = notified;
        this.stepsWalked = stepsWalked;
    }
}