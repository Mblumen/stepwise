package de.hd.stepwise.pojos;

public class TodayStepStatus {
    public final int totalSteps;
    public final int targetSteps;
    public final int progressSteps;
    public final int remainingSteps;
    public final boolean goalReached;

    public TodayStepStatus(int totalSteps, int targetSteps) {
        this.totalSteps = Math.max(0, totalSteps);
        this.targetSteps = targetSteps;
        this.progressSteps = Math.min(this.totalSteps, targetSteps);
        this.remainingSteps = Math.max(0, targetSteps - this.totalSteps);
        this.goalReached = this.totalSteps >= targetSteps;
    }
}
