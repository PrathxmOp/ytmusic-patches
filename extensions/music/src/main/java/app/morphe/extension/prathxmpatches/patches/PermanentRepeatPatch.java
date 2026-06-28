package app.morphe.extension.prathxmpatches.patches;

import app.morphe.extension.prathxmpatches.settings.Settings;

@SuppressWarnings("unused")
public class PermanentRepeatPatch {

    /**
     * Injection point
     */
    public static boolean permanentRepeat() {
        return Settings.PERMANENT_REPEAT.get();
    }
}
