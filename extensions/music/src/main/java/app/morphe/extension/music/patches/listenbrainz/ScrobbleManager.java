package app.morphe.extension.music.patches.listenbrainz;

import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import app.morphe.extension.music.settings.Settings;
import app.morphe.extension.shared.Logger;

public class ScrobbleManager {
    private static ScrobbleManager instance;

    public static synchronized ScrobbleManager getInstance() {
        if (instance == null) {
            instance = new ScrobbleManager();
        }
        return instance;
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable scrobbleRunnable;

    private String currentTitle;
    private String currentArtist;
    private String currentAlbum;
    private String currentSongId;
    private int currentDuration; // in seconds

    private long songStartedAt; // epoch in seconds
    private boolean songStarted = false;
    private long scrobbleRemainingMillis = 0L;
    private long scrobbleTimerStartedAt = 0L;

    private ScrobbleManager() {}

    public synchronized void onSetMetadata(MediaMetadata metadata) {
        if (metadata == null) return;

        try {
            String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            String album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
            String songId = metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID);
            long durationMs = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
            int duration = (int) (durationMs / 1000);

            if (title == null || title.trim().isEmpty() || artist == null || artist.trim().isEmpty()) {
                return;
            }

            // Check if it is a new song
            if (!title.equals(currentTitle) || !artist.equals(currentArtist)) {
                Logger.printInfo(() -> "ListenBrainz: new song detected: " + title + " - " + artist);
                stopScrobbleTimer();
                songStarted = false;

                currentTitle = title;
                currentArtist = artist;
                currentAlbum = album;
                currentSongId = songId;
                currentDuration = duration;
            }
        } catch (Exception e) {
            Logger.printException(() -> "ListenBrainz error parsing metadata", e);
        }
    }

    public synchronized void onSetPlaybackState(PlaybackState state) {
        if (state == null) return;
        boolean isPlaying = state.getState() == PlaybackState.STATE_PLAYING;
        onPlayerStateChanged(isPlaying);
    }

    private synchronized void onPlayerStateChanged(boolean isPlaying) {
        if (currentTitle == null || currentArtist == null) return;

        if (isPlaying) {
            if (!songStarted) {
                onSongStart();
            } else {
                onSongResume();
            }
        } else {
            onSongPause();
        }
    }

    private synchronized void onSongStart() {
        if (!Settings.LISTENBRAINZ_SCROBBLING.get()) {
            return;
        }

        songStartedAt = System.currentTimeMillis() / 1000;
        songStarted = true;

        startScrobbleTimer();

        if (Settings.LISTENBRAINZ_NOW_PLAYING.get()) {
            ListenBrainz.updateNowPlayingAsync(currentArtist, currentTitle, currentSongId, currentAlbum, currentDuration);
        }
    }

    private synchronized void onSongResume() {
        if (!Settings.LISTENBRAINZ_SCROBBLING.get()) {
            return;
        }
        if (scrobbleRemainingMillis <= 0) return;

        cancelRunnable();
        scrobbleTimerStartedAt = System.currentTimeMillis();
        scheduleScrobble(scrobbleRemainingMillis);
    }

    private synchronized void onSongPause() {
        if (!songStarted) return;
        pauseScrobbleTimer();
    }

    private synchronized void startScrobbleTimer() {
        cancelRunnable();

        int minSongDuration = Settings.LISTENBRAINZ_MIN_SONG_DURATION.get();
        if (currentDuration <= minSongDuration) {
            Logger.printInfo(() -> "ListenBrainz: duration " + currentDuration + "s <= minimum " + minSongDuration + "s, skipping scrobble");
            return;
        }

        float delayPercent = Settings.LISTENBRAINZ_DELAY_PERCENT.get() / 100.0f;
        int delaySeconds = Settings.LISTENBRAINZ_DELAY_SECONDS.get();

        long thresholdMs = (long) (currentDuration * 1000L * delayPercent);
        scrobbleRemainingMillis = Math.min(thresholdMs, (long) delaySeconds * 1000L);

        if (scrobbleRemainingMillis <= 0) {
            scrobbleSong();
            return;
        }

        scrobbleTimerStartedAt = System.currentTimeMillis();
        scheduleScrobble(scrobbleRemainingMillis);
    }

    private synchronized void pauseScrobbleTimer() {
        cancelRunnable();
        if (scrobbleTimerStartedAt != 0L) {
            long elapsed = System.currentTimeMillis() - scrobbleTimerStartedAt;
            scrobbleRemainingMillis -= elapsed;
            if (scrobbleRemainingMillis < 0) {
                scrobbleRemainingMillis = 0;
            }
            scrobbleTimerStartedAt = 0L;
        }
    }

    private synchronized void stopScrobbleTimer() {
        cancelRunnable();
        scrobbleRemainingMillis = 0L;
        scrobbleTimerStartedAt = 0L;
    }

    private void scheduleScrobble(long delayMs) {
        scrobbleRunnable = () -> {
            synchronized (ScrobbleManager.this) {
                scrobbleSong();
                scrobbleRunnable = null;
            }
        };
        handler.postDelayed(scrobbleRunnable, delayMs);
    }

    private void cancelRunnable() {
        if (scrobbleRunnable != null) {
            handler.removeCallbacks(scrobbleRunnable);
            scrobbleRunnable = null;
        }
    }

    private synchronized void scrobbleSong() {
        ListenBrainz.scrobbleAsync(currentArtist, currentTitle, songStartedAt, currentSongId, currentAlbum, currentDuration);
    }
}
