package de.hd.fitbittracks.ui;

import android.util.Pair;

import androidx.lifecycle.ViewModel;

import de.hd.fitbittracks.pojos.events.SingleLiveEvent;

public class MainSharedViewModel extends ViewModel {
    public SingleLiveEvent<Long> openTrackFinishedEvent = new SingleLiveEvent<>();
    public SingleLiveEvent<Long> openTrackWithProgressEvent = new SingleLiveEvent<>();
    public SingleLiveEvent<Long> openAchievementReachedEvent = new SingleLiveEvent<>();
}