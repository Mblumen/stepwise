package de.hd.stepwise.pojos;

import androidx.annotation.Nullable;

import java.time.LocalDate;

public class StreakSummary {
    public final int currentDays;
    public final int longestDays;
    @Nullable public final LocalDate currentStartDate;
    @Nullable public final LocalDate longestStartDate;
    @Nullable public final LocalDate longestEndDate;

    public StreakSummary(int currentDays, int longestDays,
                         @Nullable LocalDate currentStartDate,
                         @Nullable LocalDate longestStartDate,
                         @Nullable LocalDate longestEndDate) {
        this.currentDays = currentDays;
        this.longestDays = longestDays;
        this.currentStartDate = currentStartDate;
        this.longestStartDate = longestStartDate;
        this.longestEndDate = longestEndDate;
    }
}
