package de.hd.stepwise.ui.passport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.hd.stepwise.entities.Track;
import de.hd.stepwise.enums.ProgressStatus;
import de.hd.stepwise.pojos.JourneyPassport;
import de.hd.stepwise.pojos.JourneySummary;

@RunWith(AndroidJUnit4.class)
public class JourneyShareCardGeneratorTest {
    @Test
    public void generatedCardIsSharedAsSecuredPngContentUri() {
        Context context = ApplicationProvider.getApplicationContext();
        JourneyShareCardGenerator generator = new JourneyShareCardGenerator(context, Runnable::run);
        Track track = new Track();
        track.name = "Route";
        track.startLocation = "Start";
        track.endLocation = "Finish";
        JourneyPassport passport = JourneyPassport.from(new JourneySummary(17, track,
                ProgressStatus.COMPLETED, 1000, 750f, 750, 1f, null,
                1L, 2L, 1L, 1L, 0L, List.of()));
        AtomicReference<Uri> result = new AtomicReference<>();

        generator.generate(passport, result::set, exception -> {
            throw new AssertionError(exception);
        });

        Uri uri = result.get();
        assertNotNull(uri);
        assertEquals("content", uri.getScheme());
        Intent intent = JourneyShareCardGenerator.createShareIntent(uri);
        assertEquals("image/png", intent.getType());
        assertEquals(uri, intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class));
        assertTrue((intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0);
    }
}
