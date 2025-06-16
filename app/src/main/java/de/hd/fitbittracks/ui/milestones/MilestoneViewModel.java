package de.hd.fitbittracks.ui.milestones;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import de.hd.fitbittracks.daos.MilestoneDao;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.ui.BaseViewModel;

public class MilestoneViewModel extends BaseViewModel {
    private final MilestoneDao milestoneDao;
    public MilestoneViewModel(@NonNull Application application) {
        super(application);
        this.milestoneDao = AppDatabase.getInstance(getApplication()).milestoneDao();

    }

    public LiveData<Milestone> getMilestoneById(long milestoneId) {
        return milestoneDao.getMilestoneById(milestoneId);
    }
}
