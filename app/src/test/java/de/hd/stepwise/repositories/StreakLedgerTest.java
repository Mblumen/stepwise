package de.hd.stepwise.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class StreakLedgerTest {

    @Test
    public void surplusStepsProtectACompleteFutureDeficit() {
        LocalDate monday = LocalDate.of(2026, 8, 24);
        TreeMap<LocalDate, Integer> goals = new TreeMap<>();
        goals.put(monday, 5_000);

        StreakLedger.Result result = StreakLedger.calculate(
                Map.of(monday, 7_000, monday.plusDays(1), 4_500),
                goals,
                monday,
                monday.plusDays(2)
        );

        assertEquals(2, result.summary.currentDays);
        assertEquals(2, result.summary.longestDays);
        assertEquals(2, result.outcomes.size());
        assertTrue(result.outcomes.get(1).qualified);
        assertEquals(500, result.outcomes.get(1).reserveUsed);
        assertEquals(1_500, result.outcomes.get(1).reserveAfter);
    }

    @Test
    public void insufficientReserveBreaksTheStreakAndClearsTheBank() {
        LocalDate monday = LocalDate.of(2026, 8, 24);
        TreeMap<LocalDate, Integer> goals = new TreeMap<>();
        goals.put(monday, 5_000);

        StreakLedger.Result result = StreakLedger.calculate(
                Map.of(monday, 6_000, monday.plusDays(1), 3_500),
                goals,
                monday,
                monday.plusDays(2)
        );

        assertEquals(0, result.summary.currentDays);
        assertEquals(1, result.summary.longestDays);
        assertEquals(0, result.outcomes.get(1).reserveAfter);
    }

    @Test
    public void lowerGoalClampsReserveAndHigherGoalDoesNotScaleIt() {
        LocalDate monday = LocalDate.of(2026, 8, 24);
        TreeMap<LocalDate, Integer> goals = new TreeMap<>();
        goals.put(monday, 5_000);
        goals.put(monday.plusDays(1), 3_000);
        goals.put(monday.plusDays(2), 8_000);

        StreakLedger.Result result = StreakLedger.calculate(
                Map.of(monday, 10_000, monday.plusDays(1), 3_000,
                        monday.plusDays(2), 8_000),
                goals,
                monday,
                monday.plusDays(3)
        );

        assertEquals(3_000, result.outcomes.get(1).reserveAfter);
        assertEquals(3_000, result.outcomes.get(2).reserveAfter);
    }

    @Test
    public void currentDayQualifiesProvisionallyWithoutSpendingReserve() {
        LocalDate monday = LocalDate.of(2026, 8, 24);
        TreeMap<LocalDate, Integer> goals = new TreeMap<>();
        goals.put(monday, 5_000);

        StreakLedger.Result result = StreakLedger.calculate(
                Map.of(monday, 7_000, monday.plusDays(1), 5_000),
                goals,
                monday,
                monday.plusDays(1)
        );

        assertEquals(2, result.summary.currentDays);
        assertEquals(2_000, result.todayStatus.reserveSteps);
        assertEquals(2_000, result.todayStatus.projectedReserveSteps);
        assertEquals(1, result.outcomes.size());
    }

    @Test
    public void currentStreakDoesNotReportProtectionFromABrokenStreak() {
        LocalDate monday = LocalDate.of(2026, 8, 24);
        TreeMap<LocalDate, Integer> goals = new TreeMap<>();
        goals.put(monday, 5_000);

        StreakLedger.Result result = StreakLedger.calculate(
                Map.of(monday, 7_000, monday.plusDays(1), 4_500,
                        monday.plusDays(2), 0, monday.plusDays(3), 5_000),
                goals,
                monday,
                monday.plusDays(3)
        );

        assertEquals(1, result.summary.currentDays);
        assertNull(result.summary.mostRecentProtectedDate);
        assertEquals(0, result.summary.mostRecentReserveUsed);
    }
}
