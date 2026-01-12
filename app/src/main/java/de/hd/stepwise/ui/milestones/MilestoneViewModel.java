package de.hd.stepwise.ui.milestones;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import de.hd.stepwise.daos.MilestoneDao;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.pojos.MilestoneImage;
import de.hd.stepwise.repositories.MilestoneRepository;
import de.hd.stepwise.repositories.UserSettingsRepository;
import de.hd.stepwise.ui.BaseFragmentViewModel;

@HiltViewModel
public class MilestoneViewModel extends BaseFragmentViewModel {
    private final MilestoneDao milestoneDao;
    private final MilestoneRepository milestoneRepository;
    @Inject
    public MilestoneViewModel(@NonNull Application application, UserSettingsRepository userSettingsRepository, MilestoneRepository milestoneRepository) {
        super(application, userSettingsRepository);
        this.milestoneRepository = milestoneRepository;
        this.milestoneDao = db.milestoneDao();

    }

    public LiveData<MilestoneWithTotalDistance> getMilestoneById(long milestoneId) {
        return milestoneDao.getMilestoneByIdLive(milestoneId);
    }

    public void downloadMilestoneImageIfNeeded(MilestoneWithTotalDistance milestone) {
        super.downloadTrackImageIfNeeded(
                milestone.localImagePath,
                milestone.imageUrl,
                milestone.trackId,
                milestone.id,
                null,
                path -> {
                    milestoneRepository.updateMilestoneImagePath(milestone.id, path);
                }
        );
    }
    public void downloadMilestoneDetailImageIfNeeded(MilestoneImage milestoneImage, Long trackId, Long milestoneId) {
        super.downloadTrackImageIfNeeded(
                milestoneImage.localImagePath,
                milestoneImage.imageUrl,
                trackId,
                milestoneId,
                milestoneImage.position,
                path -> {
                    milestoneRepository.updateMilestoneDetailImagePath(milestoneId, milestoneImage.position, path);
                }
        );

    }
}
