package de.hd.stepwise.ui.milestones;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;

public class MilestoneAudioPlayerTest {
    @Test
    public void releaseStopsFurtherPlaybackAndIsIdempotent() throws Exception {
        FakePlayer player = new FakePlayer();
        RecordingListener listener = new RecordingListener();
        MilestoneAudioPlayer audio = new MilestoneAudioPlayer(player, "audio.mp3", listener);
        player.prepared.run();

        audio.toggle();
        assertTrue(player.playing);

        audio.release();
        audio.release();
        audio.toggle();

        assertFalse(audio.isPlaying());
        assertEquals(1, player.releaseCount);
    }

    private static final class RecordingListener implements MilestoneAudioPlayer.Listener {
        @Override public void onReady(int durationMillis) { }
        @Override public void onPlayingChanged(boolean playing) { }
        @Override public void onFailure() { }
    }

    private static final class FakePlayer implements MilestoneAudioPlayer.Player {
        Runnable prepared;
        boolean playing;
        int releaseCount;
        @Override public void setDataSource(String path) throws IOException { }
        @Override public void setOnPreparedListener(Runnable listener) { prepared = listener; }
        @Override public void setOnCompletionListener(Runnable listener) { }
        @Override public void setOnErrorListener(Runnable listener) { }
        @Override public void prepareAsync() { }
        @Override public void start() { playing = true; }
        @Override public void pause() { playing = false; }
        @Override public boolean isPlaying() { return playing; }
        @Override public int getDuration() { return 1000; }
        @Override public int getCurrentPosition() { return 50; }
        @Override public void seekTo(int positionMillis) { }
        @Override public void release() { playing = false; releaseCount++; }
    }
}
