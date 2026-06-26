package app.morphe.extension.music.patches.listenbrainz;

import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import app.morphe.extension.shared.Logger;

public class ListenBrainzHook {
    public static void onSetMetadata(MediaMetadata metadata) {
        try {
            ScrobbleManager.getInstance().onSetMetadata(metadata);
        } catch (Throwable t) {
            Logger.printException(() -> "ListenBrainzHook: onSetMetadata failed", t);
        }
    }

    public static void onSetPlaybackState(PlaybackState state) {
        try {
            ScrobbleManager.getInstance().onSetPlaybackState(state);
        } catch (Throwable t) {
            Logger.printException(() -> "ListenBrainzHook: onSetPlaybackState failed", t);
        }
    }
}
