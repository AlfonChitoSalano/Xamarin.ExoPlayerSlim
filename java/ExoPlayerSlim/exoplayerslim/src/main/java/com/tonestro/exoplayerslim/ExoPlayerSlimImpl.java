package com.tonestro.exoplayerslim;

import android.content.Context;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ExoPlayerSlimImpl implements ExoPlayerSlim {

    private final SimpleExoPlayer player;
    private final DelegatingPlayerListener delegatingPlayerListener;
    private final List<ExoPlayerSlimListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public ExoPlayerSlimImpl(Context context) {
        player = new SimpleExoPlayer.Builder(context).build();
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build(), true);
        player.setHandleAudioBecomingNoisy(true);
        player.setWakeMode(C.WAKE_MODE_NONE);
        delegatingPlayerListener = new DelegatingPlayerListener(this);
        player.addListener(delegatingPlayerListener);
    }

    @Override
    public void addListener(ExoPlayerSlimListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean removeListener(ExoPlayerSlimListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void initializeFromUrl(String url) {
        MediaItem mediaItem = MediaItem.fromUri(url);
        player.setMediaItem(mediaItem);
        player.prepare();
    }

    @Override
    public void play() {
        player.play();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void stop(boolean reset) {
        player.stop(reset);
    }

    @Override
    public void seekTo(long millis) {
        player.seekTo(millis);
    }

    @Override
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean getPlayWhenReady() {
        return player.getPlayWhenReady();
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        player.setRepeatMode(repeatMode);
    }

    @Override
    public int getRepeatMode() {
        return player.getRepeatMode();
    }

    @Override
    public void attachPlayerView(View playerView, boolean useNativeControls, int aspectRatio) {
        if (playerView == null) {
            throw new NullPointerException("playerView must not be null");
        }
        if (!(playerView instanceof PlayerView)) {
            throw new ClassCastException("playerView is not instance of PlayerView");
        }
        PlayerView actualPlayerView = (PlayerView) playerView;
        actualPlayerView.setUseController(useNativeControls);
        actualPlayerView.setResizeMode(aspectRatio);
        actualPlayerView.setPlayer(player);
    }

    @Override
    public void detachPlayerView(View playerView) {
        if (playerView == null) {
            throw new NullPointerException("playerView must not be null");
        }
        if (!(playerView instanceof PlayerView)) {
            throw new ClassCastException("playerView is not instance of PlayerView");
        }
        PlayerView actualPlayerView = (PlayerView) playerView;
        actualPlayerView.setPlayer(null);
        actualPlayerView.setUseController(false);
        actualPlayerView.setResizeMode(AspectRatios.Fit);
    }

    @Override
    public void close() {
        player.removeListener(delegatingPlayerListener);
        player.release();
    }

    private static class DelegatingPlayerListener implements Player.Listener {

        private final ExoPlayerSlimImpl exoPlayerSlim;

        public DelegatingPlayerListener(ExoPlayerSlimImpl exoPlayerSlim) {
            this.exoPlayerSlim = exoPlayerSlim;
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            exoPlayerSlim.notifyPlayerError(error);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            exoPlayerSlim.notifyIsPlayingChanged(isPlaying);
        }

        @Override
        public void onPlaybackStateChanged(int state) {
            exoPlayerSlim.notifyPlaybackStateChanged(state);
        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            exoPlayerSlim.notifyPlayWhenReadyChanged(playWhenReady, reason);
        }
    }

    private void notifyPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        for (ExoPlayerSlimListener listener : listeners) {
            listener.onPlayWhenReadyChanged(playWhenReady, reason);
        }
    }

    private void notifyPlaybackStateChanged(int state) {
        for (ExoPlayerSlimListener listener : listeners) {
            listener.onPlaybackStateChanged(state);
        }
    }

    private void notifyIsPlayingChanged(boolean isPlaying) {
        for (ExoPlayerSlimListener listener : listeners) {
            listener.onIsPlayingChanged(isPlaying);
        }
    }

    private void notifyPlayerError(Exception error) {
        for (ExoPlayerSlimListener listener : listeners) {
            listener.onPlayerError(error);
        }
    }
}
