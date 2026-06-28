package app.morphe.extension.prathxmpatches.patches;

import static app.morphe.extension.shared.Utils.hideViewBy0dpUnderCondition;

import android.view.View;

import app.morphe.extension.prathxmpatches.settings.Settings;

@SuppressWarnings("unused")
public class HideCategoryBarPatch {

    /**
     * Injection point
     */
    public static void hideCategoryBar(View view) {
        hideViewBy0dpUnderCondition(Settings.HIDE_CATEGORY_BAR, view);
    }
}
