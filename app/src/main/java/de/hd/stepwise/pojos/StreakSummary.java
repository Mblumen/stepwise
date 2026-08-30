package de.hd.stepwise.pojos;

import androidx.annotation.Nullable;

import java.time.LocalDate;

public class StreakSummary {
    public final int currentDays;
    public final int longestDays;
    @Nullable public final LocalDate currentStartDate;
    @Nullable public final LocalDate longestStartDate;
    @Nullable public final LocalDate longestEndDate;
    @Nullable public final LocalDate mostRecentProtectedDate;
    public final int mostRecentReserveUsed;
    public final int activeGoalSteps;

    public StreakSummary(int currentDays, int longestDays,
                         @Nullable LocalDate currentStartDate,
                         @Nullable LocalDate longestStartDate,
                         @Nullable LocalDate longestEndDate) {
        this(currentDays, longestDays, currentStartDate, longestStartDate, longestEndDate,
                null, 0, 5_000);
    }

    public StreakSummary(int currentDays, int longestDays,
                         @Nullable LocalDate currentStartDate,
                         @Nullable LocalDate longestStartDate,
                         @Nullable LocalDate longestEndDate,
                         @Nullable LocalDate mostRecentProtectedDate,
                         int mostRecentReserveUsed, int activeGoalSteps) {
        this.currentDays = currentDays;
        this.longestDays = longestDays;
        this.currentStartDate = currentStartDate;
        this.longestStartDate = longestStartDate;
        this.longestEndDate = longestEndDate;
        this.mostRecentProtectedDate = mostRecentProtectedDate;
        this.mostRecentReserveUsed = mostRecentReserveUsed;
        this.activeGoalSteps = activeGoalSteps;
    }
}
