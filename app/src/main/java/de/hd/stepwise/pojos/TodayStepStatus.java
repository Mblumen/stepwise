package de.hd.stepwise.pojos;

public class TodayStepStatus {
    public final int totalSteps;
    public final int targetSteps;
    public final int progressSteps;
    public final int remainingSteps;
    public final boolean goalReached;
    public final int reserveSteps;
    public final boolean reserveSufficient;
    public final int projectedReserveSteps;
    public final int projectedReserveAdded;

    public TodayStepStatus(int totalSteps, int targetSteps) {
        this(totalSteps, targetSteps, 0);
    }

    public TodayStepStatus(int totalSteps, int targetSteps, int reserveSteps) {
        this.totalSteps = Math.max(0, totalSteps);
        this.targetSteps = targetSteps;
        this.progressSteps = Math.min(this.totalSteps, targetSteps);
        this.remainingSteps = Math.max(0, targetSteps - this.totalSteps);
        this.goalReached = this.totalSteps >= targetSteps;
        this.reserveSteps = Math.min(Math.max(0, reserveSteps), targetSteps);
        this.reserveSufficient = !goalReached && this.reserveSteps >= this.remainingSteps;
        this.projectedReserveSteps = goalReached
                ? Math.min(targetSteps, this.reserveSteps + this.totalSteps - targetSteps)
                : this.reserveSteps;
        this.projectedReserveAdded = this.projectedReserveSteps - this.reserveSteps;
    }
}
