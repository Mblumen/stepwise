package de.hd.stepwise.widget;

import android.content.Context;
import android.content.Intent;

import de.hd.stepwise.MainActivity;

public final class JourneyWidgetIntents {
    public static final String DESTINATION_PROGRESS = "progress_fragment";
    public static final String DESTINATION_TRACKS = "tracks_fragment";

    private JourneyWidgetIntents() { }

    public static Intent progress(Context context, long progressId) {
        return new Intent(context, MainActivity.class)
                .putExtra("navigate_to", DESTINATION_PROGRESS)
                .putExtra("progress_id", progressId);
    }

    public static Intent tracks(Context context) {
        return new Intent(context, MainActivity.class)
                .putExtra("navigate_to", DESTINATION_TRACKS);
    }
}
