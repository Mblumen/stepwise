package de.hd.stepwise.ui.tracks;

import android.app.Application;

import androidx.annotation.NonNull;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.repositories.MilestoneRepository;
import de.hd.stepwise.repositories.TrackRepository;
import de.hd.stepwise.repositories.UserSettingsRepository;
import de.hd.stepwise.ui.BaseFragmentViewModel;

public abstract class BaseTracksViewModel extends BaseFragmentViewModel {
    protected final TrackRepository trackRepository;
    protected final MilestoneRepository milestoneRepository;
    public BaseTracksViewModel(@NonNull Application application, UserSettingsRepository userSettingsRepository, TrackRepository trackRepository, MilestoneRepository milestoneRepository) {
        super(application, userSettingsRepository);
        this.trackRepository = trackRepository;
        this.milestoneRepository = milestoneRepository;
    }

    public void downloadTrackImageIfNeeded(Track track) {
        super.downloadTrackImageIfNeeded(
                track.localImagePath,
                track.imageUrl,
                track.id,
                null,
                null
                , path -> {
                    trackRepository.updateTrackImagePath(track.id, path);
                }
        );
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
}
