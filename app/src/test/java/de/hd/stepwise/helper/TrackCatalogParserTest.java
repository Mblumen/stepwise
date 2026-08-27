package de.hd.stepwise.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.hd.stepwise.dtos.TrackJson;

public class TrackCatalogParserTest {

    @Test
    public void legacyTrackJsonParsesWithoutRichMilestoneContent() {
        TrackJson track = new TrackCatalogParser().parse("""
                {
                  "name": "Legacy",
                  "startLocation": "A",
                  "endLocation": "B",
                  "milestones": [{
                    "distanceOffsetToPrevious": 1000,
                    "title": "Stop",
                    "description": "Description"
                  }]
                }
                """);

        assertEquals("Legacy", track.name);
        assertEquals(1, track.milestones.size());
        assertNull(track.milestones.get(0).audioUrl);
        assertNull(track.milestones.get(0).stampImageUrl);
        assertNull(track.milestones.get(0).discovery);
        assertNull(track.milestones.get(0).quiz);
    }

    @Test
    public void validRichMilestoneContentIsPreserved() {
        TrackJson track = new TrackCatalogParser().parse("""
                {
                  "name": "Rich",
                  "startLocation": "A",
                  "endLocation": "B",
                  "milestones": [{
                    "distanceOffsetToPrevious": 1000,
                    "title": "Stop",
                    "description": "Description",
                    "audioUrl": "https://example.com/audio.mp3",
                    "stampImageUrl": "https://example.com/stamp.png",
                    "discovery": {"title": "Fact", "text": "Useful", "sourceUrl": "https://example.com"},
                    "quiz": {"question": "Question?", "answers": ["No", "Yes"], "correctAnswerIndex": 1, "explanation": "Because"}
                  }]
                }
                """);

        assertEquals("https://example.com/audio.mp3", track.milestones.get(0).audioUrl);
        assertEquals("https://example.com/stamp.png", track.milestones.get(0).stampImageUrl);
        assertNotNull(track.milestones.get(0).discovery);
        assertNotNull(track.milestones.get(0).quiz);
    }

    @Test
    public void malformedOptionalContentDoesNotRejectTrack() {
        TrackJson track = new TrackCatalogParser().parse("""
                {
                  "name": "Partially Rich",
                  "startLocation": "A",
                  "endLocation": "B",
                  "milestones": [{
                    "distanceOffsetToPrevious": 1000,
                    "title": "Stop",
                    "description": "Description",
                    "audioUrl": " ",
                    "stampImageUrl": "",
                    "discovery": {"title": "Fact", "text": " "},
                    "quiz": {"question": "Question?", "answers": ["Only one"], "correctAnswerIndex": 4}
                  }]
                }
                """);

        assertEquals("Partially Rich", track.name);
        assertEquals(1, track.milestones.size());
        assertNull(track.milestones.get(0).audioUrl);
        assertNull(track.milestones.get(0).stampImageUrl);
        assertNull(track.milestones.get(0).discovery);
        assertNull(track.milestones.get(0).quiz);
    }
}
