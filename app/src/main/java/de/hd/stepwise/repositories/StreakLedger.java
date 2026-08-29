package de.hd.stepwise.repositories;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import de.hd.stepwise.pojos.StreakSummary;
import de.hd.stepwise.pojos.TodayStepStatus;

public final class StreakLedger {
    private StreakLedger() {}

    public static Result calculate(Map<LocalDate, Integer> stepsByDate,
                                   NavigableMap<LocalDate, Integer> goalsByDate,
                                   LocalDate activationDate,
                                   LocalDate today) {
        List<Outcome> outcomes = new ArrayList<>();
        int reserve = 0;
        LocalDate date = activationDate;
        while (date.isBefore(today)) {
            int goal = goalFor(goalsByDate, date);
            reserve = Math.min(reserve, goal);
            int actualSteps = Math.max(0, stepsByDate.getOrDefault(date, 0));
            int reserveUsed = 0;
            boolean qualified;
            if (actualSteps >= goal) {
                qualified = true;
                reserve = Math.min(goal, reserve + actualSteps - goal);
            } else {
                int deficit = goal - actualSteps;
                qualified = reserve >= deficit;
                if (qualified) {
                    reserveUsed = deficit;
                    reserve -= deficit;
                } else {
                    reserve = 0;
                }
            }
            outcomes.add(new Outcome(date, goal, qualified, reserveUsed, reserve));
            date = date.plusDays(1);
        }

        int todayGoal = goalFor(goalsByDate, today);
        reserve = Math.min(reserve, todayGoal);
        int todaySteps = Math.max(0, stepsByDate.getOrDefault(today, 0));
        TodayStepStatus todayStatus = new TodayStepStatus(todaySteps, todayGoal, reserve);
        StreakSummary summary = summarize(outcomes, today, todaySteps >= todayGoal, todayGoal);
        return new Result(outcomes, summary, todayStatus);
    }

    private static int goalFor(NavigableMap<LocalDate, Integer> goalsByDate, LocalDate date) {
        Map.Entry<LocalDate, Integer> goal = goalsByDate.floorEntry(date);
        if (goal == null) goal = goalsByDate.firstEntry();
        return goal.getValue();
    }

    private static StreakSummary summarize(List<Outcome> outcomes, LocalDate today,
                                           boolean todayQualifies, int todayGoal) {
        List<LocalDate> qualifyingDates = new ArrayList<>();
        for (Outcome outcome : outcomes) {
            if (outcome.qualified) qualifyingDates.add(outcome.date);
        }
        if (todayQualifies) qualifyingDates.add(today);

        int longestDays = 0;
        LocalDate longestStart = null;
        LocalDate longestEnd = null;
        int runDays = 0;
        LocalDate runStart = null;
        LocalDate previous = null;
        for (LocalDate qualifyingDate : qualifyingDates) {
            if (previous == null || ChronoUnit.DAYS.between(previous, qualifyingDate) != 1) {
                runDays = 1;
                runStart = qualifyingDate;
            } else {
                runDays++;
            }
            if (runDays > longestDays) {
                longestDays = runDays;
                longestStart = runStart;
                longestEnd = qualifyingDate;
            }
            previous = qualifyingDate;
        }

        LocalDate currentEnd = qualifyingDates.isEmpty()
                ? null : qualifyingDates.get(qualifyingDates.size() - 1);
        int currentDays = 0;
        LocalDate currentStart = null;
        if (currentEnd != null
                && (currentEnd.equals(today) || currentEnd.equals(today.minusDays(1)))) {
            currentDays = 1;
            currentStart = currentEnd;
            for (int index = qualifyingDates.size() - 2; index >= 0; index--) {
                LocalDate qualifyingDate = qualifyingDates.get(index);
                if (ChronoUnit.DAYS.between(qualifyingDate, currentStart) != 1) break;
                currentStart = qualifyingDate;
                currentDays++;
            }
        }
        LocalDate mostRecentProtectedDate = null;
        int mostRecentReserveUsed = 0;
        if (currentStart != null) {
            for (Outcome outcome : outcomes) {
                if (!outcome.date.isBefore(currentStart) && outcome.reserveUsed > 0) {
                    mostRecentProtectedDate = outcome.date;
                    mostRecentReserveUsed = outcome.reserveUsed;
                }
            }
        }
        return new StreakSummary(currentDays, longestDays, currentStart, longestStart, longestEnd,
                mostRecentProtectedDate, mostRecentReserveUsed, todayGoal);
    }

    public static final class Outcome {
        public final LocalDate date;
        public final int goalSteps;
        public final boolean qualified;
        public final int reserveUsed;
        public final int reserveAfter;

        public Outcome(LocalDate date, int goalSteps, boolean qualified,
                       int reserveUsed, int reserveAfter) {
            this.date = date;
            this.goalSteps = goalSteps;
            this.qualified = qualified;
            this.reserveUsed = reserveUsed;
            this.reserveAfter = reserveAfter;
        }
    }

    public static final class Result {
        public final List<Outcome> outcomes;
        public final StreakSummary summary;
        public final TodayStepStatus todayStatus;

        public Result(List<Outcome> outcomes, StreakSummary summary,
                      TodayStepStatus todayStatus) {
            this.outcomes = outcomes;
            this.summary = summary;
            this.todayStatus = todayStatus;
        }
    }
}
