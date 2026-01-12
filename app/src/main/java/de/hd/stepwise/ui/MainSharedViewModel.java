package de.hd.stepwise.ui;

import androidx.lifecycle.ViewModel;

import de.hd.stepwise.pojos.events.SingleLiveEvent;

public class MainSharedViewModel extends ViewModel {
    public SingleLiveEvent<Long> openTrackFinishedEvent = new SingleLiveEvent<>();
    public SingleLiveEvent<Long> openTrackWithProgressEvent = new SingleLiveEvent<>();
    public SingleLiveEvent<Long> openAchievementReachedEvent = new SingleLiveEvent<>();
}