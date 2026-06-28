/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.extension.prathxmpatches.patches;

import app.morphe.extension.prathxmpatches.settings.Settings;

@SuppressWarnings("unused")
public class HideAdsPatch {

    /**
     * Injection point.
     */
    public static boolean hideGetPremiumLabel() {
        return Settings.HIDE_GET_PREMIUM_LABEL.get();
    }
}