package de.hd.fitbittracks.ui.achievements;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.fitbittracks.daos.AchievementDao;
import de.hd.fitbittracks.daos.AppRecordDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.entities.AppRecord;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.repositories.AchievementRepository;
import de.hd.fitbittracks.repositories.UserSettingsRepository;
import de.hd.fitbittracks.ui.BaseViewModel;

@HiltViewModel
public class AchievementsViewModel extends BaseViewModel {
    private final AchievementDao achievementDao;
    private final AppRecordDao appRecordDao;
    private final LiveData<List<ListItem>> allAchievements;
    private final LiveData<List<AppRecord>> allAppRecords;

    private final MutableLiveData<AchievementFilter> achievementLiveData = new MutableLiveData<>();
    @Inject
    public AchievementsViewModel(@NonNull Application application, AchievementRepository achievementRepository, UserSettingsRepository userSettingsRepository) {
        super(application, userSettingsRepository);
        achievementDao = AppDatabase.getInstance(application).achievementDao();
        appRecordDao = AppDatabase.getInstance(application).appRecordDao();
        allAchievements = achievementRepository.getAchievementsWithSeparators();
        allAppRecords = appRecordDao.getAll();

    }

    public LiveData<List<ListItem>> getAllAchievements() {
        return allAchievements;
    }
    public LiveData<List<AppRecord>> getAllAppRecords() {
        return allAppRecords;
    }

    public LiveData<Achievement> getAchiementById(long id) {
        return achievementDao.getById(id);
    }

    public void setAchievementFilter(AchievementFilter filter) {
        achievementLiveData.setValue(filter);
    }
}
