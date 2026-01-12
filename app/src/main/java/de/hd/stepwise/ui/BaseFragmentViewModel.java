package de.hd.stepwise.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.helper.DownloadHelper;
import de.hd.stepwise.repositories.UserSettingsRepository;

public abstract class BaseFragmentViewModel extends UpdateViewModel {
    protected final LiveData<UserSettings> settings;
    protected final LiveData<Float> stepLength;
    private final Set<String> downloading = Collections.synchronizedSet(new HashSet<>());

    public BaseFragmentViewModel(@NonNull Application application, UserSettingsRepository userSettingsRepository) {
        super(application);
        settings = userSettingsRepository.getSettings();
        stepLength = userSettingsRepository.getStepLength();
    }

    public LiveData<UserSettings> getSettings() {
        return settings;
    }

    public LiveData<Float> getStepLength() {
        return stepLength;
    }

    protected void downloadTrackImageIfNeeded(String localImagePath, String imageUrl, Long trackId, Long milestoneId, Long milestoneImageId, Consumer<String> saveCallback) {
        if (localImagePath != null && !localImagePath.isEmpty()) return;
        StringBuilder trackIdBuilder = new StringBuilder();
        trackIdBuilder.append("track_").append(trackId);
        if (milestoneId != null) {
            trackIdBuilder.append("_milestone_").append(milestoneId);
            if (milestoneImageId != null) {
                trackIdBuilder.append("_image_").append(milestoneImageId);
            }
        }
        if (downloading.contains(trackIdBuilder.toString())) return;

        downloading.add(trackIdBuilder.toString());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String path = DownloadHelper.downloadTrackImage(
                        getApplication(),
                        imageUrl,
                        trackId,
                        milestoneId,
                        milestoneImageId
                );
                saveCallback.accept(path);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                downloading.remove(trackIdBuilder.toString());
            }
        });
    }
}
