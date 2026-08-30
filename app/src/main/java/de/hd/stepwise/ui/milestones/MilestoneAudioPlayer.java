package de.hd.stepwise.ui.milestones;

import android.media.MediaPlayer;

import java.io.IOException;

final class MilestoneAudioPlayer {
    interface Listener {
        void onReady(int durationMillis);
        void onPlayingChanged(boolean playing);
        void onFailure();
    }

    interface Player {
        void setDataSource(String path) throws IOException;
        void setOnPreparedListener(Runnable listener);
        void setOnCompletionListener(Runnable listener);
        void setOnErrorListener(Runnable listener);
        void prepareAsync();
        void start();
        void pause();
        boolean isPlaying();
        int getDuration();
        int getCurrentPosition();
        void seekTo(int positionMillis);
        void release();
    }

    private final Player player;
    private final Listener listener;
    private boolean ready;
    private boolean released;

    MilestoneAudioPlayer(String path, Listener listener) throws IOException {
        this(new AndroidPlayer(), path, listener);
    }

    MilestoneAudioPlayer(Player player, String path, Listener listener) throws IOException {
        this.player = player;
        this.listener = listener;
        player.setOnPreparedListener(() -> {
            if (released) return;
            ready = true;
            listener.onReady(player.getDuration());
        });
        player.setOnCompletionListener(() -> {
            if (!released) listener.onPlayingChanged(false);
        });
        player.setOnErrorListener(() -> {
            if (!released) listener.onFailure();
        });
        player.setDataSource(path);
        player.prepareAsync();
    }

    void toggle() {
        if (!ready || released) return;
        if (player.isPlaying()) {
            player.pause();
            listener.onPlayingChanged(false);
        } else {
            player.start();
            listener.onPlayingChanged(true);
        }
    }

    void seekTo(int positionMillis) {
        if (ready && !released) player.seekTo(positionMillis);
    }

    int getCurrentPosition() {
        return ready && !released ? player.getCurrentPosition() : 0;
    }

    boolean isPlaying() {
        return ready && !released && player.isPlaying();
    }

    void release() {
        if (released) return;
        released = true;
        ready = false;
        player.release();
    }

    private static final class AndroidPlayer implements Player {
        private final MediaPlayer delegate = new MediaPlayer();

        @Override public void setDataSource(String path) throws IOException { delegate.setDataSource(path); }
        @Override public void setOnPreparedListener(Runnable listener) {
            delegate.setOnPreparedListener(unused -> listener.run());
        }
        @Override public void setOnCompletionListener(Runnable listener) {
            delegate.setOnCompletionListener(unused -> listener.run());
        }
        @Override public void setOnErrorListener(Runnable listener) {
            delegate.setOnErrorListener((unused, what, extra) -> { listener.run(); return true; });
        }
        @Override public void prepareAsync() { delegate.prepareAsync(); }
        @Override public void start() { delegate.start(); }
        @Override public void pause() { delegate.pause(); }
        @Override public boolean isPlaying() { return delegate.isPlaying(); }
        @Override public int getDuration() { return delegate.getDuration(); }
        @Override public int getCurrentPosition() { return delegate.getCurrentPosition(); }
        @Override public void seekTo(int positionMillis) { delegate.seekTo(positionMillis); }
        @Override public void release() { delegate.release(); }
    }
}
