package de.hd.stepwise.pojos;

import androidx.annotation.Nullable;

import java.time.LocalDate;

public class DailyGoalState {
    public final int activeSteps;
    @Nullable public final Integer pendingSteps;
    @Nullable public final LocalDate pendingEffectiveDate;

    public DailyGoalState(int activeSteps, @Nullable Integer pendingSteps,
                          @Nullable LocalDate pendingEffectiveDate) {
        this.activeSteps = activeSteps;
        this.pendingSteps = pendingSteps;
        this.pendingEffectiveDate = pendingEffectiveDate;
    }
}
