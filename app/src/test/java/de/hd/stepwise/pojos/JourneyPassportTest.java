package de.hd.stepwise.pojos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.List;

import de.hd.stepwise.entities.Track;
import de.hd.stepwise.enums.ProgressStatus;

public class JourneyPassportTest {
    @Test
    public void completedProgressBecomesPassportForItsOwnProgressId() {
        Track track = new Track();
        track.name = "Alpine Route";
        JourneySummary summary = new JourneySummary(42, track, ProgressStatus.COMPLETED,
                12_345, 9_876f, 10_000, 0.98f, null,
                1_000L, 9_000L, 8_000L, 7_000L, 1_000L, List.of());

        JourneyPassport passport = JourneyPassport.from(summary);

        assertEquals(42L, passport.progressId);
        assertEquals("Alpine Route", passport.track.name);
        assertEquals(9_876f, passport.distanceWalked, 0f);
    }

    @Test
    public void activeAndMissingJourneysDoNotProducePassports() {
        Track track = new Track();
        JourneySummary active = new JourneySummary(7, track, ProgressStatus.ACTIVE,
                0, 0, 0, 0, null, null, null, null, null, null, List.of());

        assertNull(JourneyPassport.from(null));
        assertNull(JourneyPassport.from(active));
    }

    @Test
    public void missingLegacyTrackMetadataHasSafeDisplayFallbacks() {
        Track track = new Track();
        track.name = " ";
        track.startLocation = null;
        track.endLocation = "";
        JourneyPassport passport = JourneyPassport.from(new JourneySummary(8, track,
                ProgressStatus.COMPLETED, 0, 0, 0, 0, null,
                0L, 0L, 0L, 0L, 0L, List.of()));

        assertEquals("Unnamed journey", passport.displayTrackName("Unnamed journey"));
        assertEquals("Unknown start", passport.displayStart("Unknown start"));
        assertEquals("Unknown destination", passport.displayDestination("Unknown destination"));
    }
}
