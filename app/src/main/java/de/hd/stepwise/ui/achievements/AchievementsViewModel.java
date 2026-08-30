package de.hd.stepwise.ui.achievements;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.stepwise.daos.AchievementDao;
import de.hd.stepwise.daos.AppRecordDao;
import de.hd.stepwise.entities.Achievement;
import de.hd.stepwise.entities.AppRecord;
import de.hd.stepwise.pojos.ListItem;
import de.hd.stepwise.repositories.AchievementRepository;
import de.hd.stepwise.repositories.UserSettingsRepository;
import de.hd.stepwise.repositories.DailyActivityRepository;
import de.hd.stepwise.pojos.StreakSummary;
import de.hd.stepwise.pojos.TodayStepStatus;
import de.hd.stepwise.enums.RecordType;
import java.time.ZoneId;
import java.util.ArrayList;
import java.text.NumberFormat;
import java.util.Locale;
import de.hd.stepwise.ui.BaseFragmentViewModel;

@HiltViewModel
public class AchievementsViewModel extends BaseFragmentViewModel {
    private final AchievementDao achievementDao;
    private final LiveData<List<ListItem>> allAchievements;
    private final LiveData<List<AppRecord>> allAppRecords;
    private final MutableLiveData<java.time.LocalDate> today =
            new MutableLiveData<>(java.time.LocalDate.now());
    private final LiveData<TodayStepStatus> todayStepStatus;
    private final DailyActivityRepository dailyActivityRepository;

    private final MutableLiveData<AchievementFilter> achievementLiveData = new MutableLiveData<>();
    @Inject
    public AchievementsViewModel(@NonNull Application application, AchievementRepository achievementRepository,
                                 UserSettingsRepository userSettingsRepository,
                                 DailyActivityRepository dailyActivityRepository) {
        super(application, userSettingsRepository);
        this.dailyActivityRepository = dailyActivityRepository;
        achievementDao = db.achievementDao();
        AppRecordDao appRecordDao = db.appRecordDao();
        allAchievements = achievementRepository.getAchievementsWithSeparators();
        LiveData<StreakSummary> streakSummary = Transformations.switchMap(today,
                dailyActivityRepository::observeStreakSummary);
        allAppRecords = combineRecords(appRecordDao.getAll(), streakSummary);
        todayStepStatus = Transformations.switchMap(today,
                dailyActivityRepository::observeTodayStatus);

    }

    private LiveData<List<AppRecord>> combineRecords(LiveData<List<AppRecord>> storedRecords,
                                                     LiveData<StreakSummary> streakSummary) {
        MediatorLiveData<List<AppRecord>> result = new MediatorLiveData<>();
        final List<AppRecord>[] stored = new List[]{List.of()};
        final StreakSummary[] streak = new StreakSummary[]{null};
        Runnable publish = () -> {
            if (streak[0] == null) return;
            List<AppRecord> combined = new ArrayList<>(stored[0]);
            combined.add(currentStreakRecord(streak[0]));
            combined.add(longestStreakRecord(streak[0]));
            result.setValue(combined);
        };
        result.addSource(storedRecords, records -> {
            stored[0] = records == null ? List.of() : records;
            publish.run();
        });
        result.addSource(streakSummary, summary -> {
            streak[0] = summary;
            publish.run();
        });
        return result;
    }

    private AppRecord currentStreakRecord(StreakSummary summary) {
        AppRecord record = new AppRecord("Current streak", "days", RecordType.STREAK);
        record.id = Long.MIN_VALUE + 1;
        record.value = summary.currentDays;
        record.timestamp = startOfDay(summary.currentStartDate);
        record.description = summary.currentStartDate == null
                ? "Walk at least " + formatSteps(summary.activeGoalSteps)
                    + " steps in a day to start a streak."
                : currentStreakDescription(summary);
        return record;
    }

    private AppRecord longestStreakRecord(StreakSummary summary) {
        AppRecord record = new AppRecord("Longest streak", "days", RecordType.STREAK);
        record.id = Long.MIN_VALUE + 2;
        record.value = summary.longestDays;
        record.timestamp = startOfDay(summary.longestEndDate);
        record.description = summary.longestStartDate == null
                ? "Walk at least " + formatSteps(summary.activeGoalSteps)
                    + " steps in a day to set a streak record."
                : "Daily goals met from " + summary.longestStartDate
                    + " through " + summary.longestEndDate + ".";
        return record;
    }

    private String currentStreakDescription(StreakSummary summary) {
        String description = "Daily goals met since " + summary.currentStartDate + ".";
        if (summary.mostRecentProtectedDate != null) {
            description += " Reserve last used on " + summary.mostRecentProtectedDate
                    + ": " + formatSteps(summary.mostRecentReserveUsed) + " steps.";
        }
        return description;
    }

    private String formatSteps(int steps) {
        return NumberFormat.getIntegerInstance(Locale.getDefault()).format(steps);
    }

    private long startOfDay(java.time.LocalDate date) {
        return date == null ? 0 : date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public LiveData<List<ListItem>> getAllAchievements() {
        return allAchievements;
    }
    public LiveData<List<AppRecord>> getAllAppRecords() {
        return allAppRecords;
    }

    public LiveData<TodayStepStatus> getTodayStepStatus() {
        return todayStepStatus;
    }

    public void setToday(java.time.LocalDate date) {
        dailyActivityRepository.refreshStreakLedger(date);
        if (!date.equals(today.getValue())) {
            today.setValue(date);
        }
    }

    public LiveData<Achievement> getAchiementById(long id) {
        return achievementDao.getById(id);
    }

    public void setAchievementFilter(AchievementFilter filter) {
        achievementLiveData.setValue(filter);
    }
}
