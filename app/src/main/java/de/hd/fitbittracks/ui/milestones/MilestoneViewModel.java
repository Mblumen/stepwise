package de.hd.fitbittracks.ui.milestones;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.repositories.UserSettingsRepository;
import de.hd.fitbittracks.ui.BaseViewModel;

@HiltViewModel
public class MilestoneViewModel extends BaseViewModel {
    private final MilestoneDao milestoneDao;
    @Inject
    public MilestoneViewModel(@NonNull Application application, UserSettingsRepository userSettingsRepository) {
        super(application, userSettingsRepository);
        this.milestoneDao = AppDatabase.getInstance(getApplication()).milestoneDao();

    }

    public LiveData<MilestoneWithTotalDistance> getMilestoneById(long milestoneId) {
        return milestoneDao.getMilestoneById(milestoneId);
    }
}
