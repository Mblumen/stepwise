package de.hd.stepwise.pojos.events;

import android.util.Pair;

import de.hd.stepwise.entities.Track;
import de.hd.stepwise.entities.UserProgress;

public class TrackWithProgressEvent extends MessageEvent<Pair<Track, UserProgress>> {
    public TrackWithProgressEvent(Pair<Track, UserProgress> content, String message) {
        super(content, message);
    }

    public TrackWithProgressEvent(Pair<Track, UserProgress> content) {
        super(content);
    }
}
