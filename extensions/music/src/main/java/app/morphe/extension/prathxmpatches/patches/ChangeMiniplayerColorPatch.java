package app.morphe.extension.prathxmpatches.patches;

import app.morphe.extension.prathxmpatches.settings.Settings;

@SuppressWarnings("unused")
public class ChangeMiniplayerColorPatch {

    /**
     * Injection point
     */
    public static boolean changeMiniplayerColor() {
        return Settings.CHANGE_MINIPLAYER_COLOR.get();
    }
}
