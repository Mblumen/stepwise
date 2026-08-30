package de.hd.stepwise.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;
import android.content.ContextWrapper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.hd.stepwise.MainActivity;

@RunWith(AndroidJUnit4.class)
public class JourneyWidgetIntentsTest {
    @Test
    public void journeyTargetOpensMatchingProgress() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = JourneyWidgetIntents.progress(context, 91);

        assertEquals(MainActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(JourneyWidgetIntents.DESTINATION_PROGRESS,
                intent.getStringExtra("navigate_to"));
        assertEquals(91L, intent.getLongExtra("progress_id", -1));
    }

    @Test
    public void emptyTargetOpensTracks() {
        Intent intent = JourneyWidgetIntents.tracks(ApplicationProvider.getApplicationContext());
        assertEquals(JourneyWidgetIntents.DESTINATION_TRACKS,
                intent.getStringExtra("navigate_to"));
    }

    @Test
    public void refreshRequestTargetsOnlyTheWidgetProvider() {
        RecordingContext context = new RecordingContext(
                ApplicationProvider.getApplicationContext());

        JourneyWidgetUpdater.requestUpdate(context);

        assertNotNull(context.broadcast);
        assertEquals(JourneyWidgetUpdater.ACTION_REFRESH, context.broadcast.getAction());
        assertEquals(JourneyWidgetProvider.class.getName(),
                context.broadcast.getComponent().getClassName());
    }

    private static class RecordingContext extends ContextWrapper {
        Intent broadcast;

        RecordingContext(Context base) {
            super(base);
        }

        @Override
        public void sendBroadcast(Intent intent) {
            broadcast = intent;
        }
    }
}
