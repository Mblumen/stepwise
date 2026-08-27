package de.hd.stepwise.entities;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(
        tableName = "reached_milestone",
        primaryKeys = {"progressId", "milestoneId"}
)
public class ReachedMilestone {
    public long progressId;
    public long milestoneId;
    public int stepsWalked;
    public long reachedAt;
    public Integer selectedQuizAnswer;
    public Long quizCompletedAt;

    @Ignore
    public ReachedMilestone(long progressId, long milestoneId, int stepsWalked) {
        this(progressId, milestoneId, stepsWalked, System.currentTimeMillis());
    }

    public ReachedMilestone(long progressId, long milestoneId, int stepsWalked, long reachedAt) {
        this.progressId = progressId;
        this.milestoneId = milestoneId;
        this.stepsWalked = stepsWalked;
        this.reachedAt = reachedAt;
    }
}
