package app.morphe.extension.prathxmpatches.patches;

import app.morphe.extension.prathxmpatches.settings.Settings;

@SuppressWarnings("unused")
public class ForceOriginalAudioPatch {

    /**
     * Injection point.
     */
    public static void setEnabled() {
        app.morphe.extension.shared.patches.ForceOriginalAudioPatch.setEnabled(
                Settings.FORCE_ORIGINAL_AUDIO.get(),
                Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get()
        );
    }
}
