package app.morphe.extension.prathxmpatches.patches;

import app.morphe.extension.prathxmpatches.settings.Settings;

@SuppressWarnings("unused")
public class HideVideoAdsPatch {

    /**
     * Injection point
     */
    public static boolean showVideoAds(boolean original) {
        if (Settings.HIDE_VIDEO_ADS.get()) {
            return false;
        }
        return original;
    }
}
