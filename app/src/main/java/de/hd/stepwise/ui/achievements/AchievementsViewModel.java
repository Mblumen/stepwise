package de.hd.stepwise.ui.achievements;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import de.hd.stepwise.ui.BaseFragmentViewModel;

@HiltViewModel
public class AchievementsViewModel extends BaseFragmentViewModel {
    private final AchievementDao achievementDao;
    private final LiveData<List<ListItem>> allAchievements;
    private final LiveData<List<AppRecord>> allAppRecords;

    private final MutableLiveData<AchievementFilter> achievementLiveData = new MutableLiveData<>();
    @Inject
    public AchievementsViewModel(@NonNull Application application, AchievementRepository achievementRepository, UserSettingsRepository userSettingsRepository) {
        super(application, userSettingsRepository);
        achievementDao = db.achievementDao();
        AppRecordDao appRecordDao = db.appRecordDao();
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
