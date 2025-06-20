package de.hd.fitbittracks.pojos.events;

import android.util.Pair;

import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;

public class TrackWithProgressEvent extends MessageEvent<Pair<Track, UserProgress>> {
    public TrackWithProgressEvent(Pair<Track, UserProgress> content, String message) {
        super(content, message);
    }

    public TrackWithProgressEvent(Pair<Track, UserProgress> content) {
        super(content);
    }
}
