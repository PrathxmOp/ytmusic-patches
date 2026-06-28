/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.extension.music.settings.preference;

import static app.morphe.extension.shared.StringRef.str;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.morphe.extension.music.discord.DiscordRpcManager;
import app.morphe.extension.music.discord.DiscordTokenStore;
import app.morphe.extension.shared.Utils;
import app.morphe.extension.shared.ui.CustomDialog;
import app.morphe.extension.shared.ui.Dim;

@SuppressWarnings({"unused", "deprecation"})
public class DiscordTokenPreference extends Preference {

    private static final int STATUS_COLOR_ERROR = 0xFFE53935;
    private static final int STATUS_COLOR_SUCCESS = 0xFF43A047;

    public DiscordTokenPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public DiscordTokenPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DiscordTokenPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DiscordTokenPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        DiscordTokenStore.INSTANCE.init(getContext());
        setSelectable(true);
        setPersistent(false);
        updateSummary();
    }

    private void updateSummary() {
        setSummary(str(isLoggedIn()
                ? "morphe_music_discord_rpc_token_summary_logged_in"
                : "morphe_music_discord_rpc_token_summary_logged_out"
        ));
    }

    private boolean isLoggedIn() {
        DiscordTokenStore.INSTANCE.init(getContext());
        String token = DiscordTokenStore.INSTANCE.retrieve();
        return token != null && !token.isEmpty();
    }

    @Override
    protected void onClick() {
        showDialog();
    }

    private void showDialog() {
        Context context = getContext();
        final boolean loggedIn = isLoggedIn();

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView instruction = new TextView(context);
        instruction.setText(str("morphe_music_discord_rpc_token_dialog_message"));
        instruction.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        instruction.setTextColor(Utils.getAppForegroundColor());
        LinearLayout.LayoutParams instructionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        instructionParams.bottomMargin = Dim.dp12;
        content.addView(instruction, instructionParams);

        TextView status = new TextView(context);
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        status.setTextColor(Utils.getAppForegroundColor());
        status.setVisibility(View.GONE);

        // We check if the activity is available for context, which must be GoogleApiActivity.
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        final Activity finalActivity = activity;

        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                str("morphe_music_discord_rpc_token_dialog_title"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );

        Dialog dialog = dialogPair.first;
        LinearLayout mainLayout = dialogPair.second;

        Button linkBtn = CustomDialog.createButton(context, null,
                str(loggedIn ? "morphe_music_discord_rpc_token_dialog_logout_btn" : "morphe_music_discord_rpc_token_dialog_link_btn"),
                () -> {
                    if (loggedIn) {
                        DiscordRpcManager.INSTANCE.logout();
                        updateSummary();
                        Utils.showToastShort("Logged out successfully");
                        dialog.dismiss();
                    } else {
                        if (finalActivity == null) {
                            showStatus(status, "Context is not an Activity", STATUS_COLOR_ERROR);
                            return;
                        }
                        showStatus(status, str("morphe_music_discord_rpc_token_status_linking"), Utils.getAppForegroundColor());
                        DiscordRpcManager.INSTANCE.authorize(finalActivity, (success) -> {
                            updateSummary();
                            if (success) {
                                showStatus(status, str("morphe_music_discord_rpc_token_status_success"), STATUS_COLOR_SUCCESS);
                                Utils.showToastShort(str("morphe_music_discord_rpc_token_status_success"));
                                dialog.dismiss();
                            } else {
                                String errorMsg = "Auth failed or cancelled";
                                showStatus(status, str("morphe_music_discord_rpc_token_status_error", errorMsg), STATUS_COLOR_ERROR);
                            }
                            return kotlin.Unit.INSTANCE;
                        });
                    }
                },
                false, false);

        Button cancelBtn = CustomDialog.createButton(context, null,
                str("morphe_music_discord_rpc_token_dialog_cancel_btn"),
                () -> dialog.dismiss(),
                false, false);

        LinearLayout actionRow = new LinearLayout(context);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        actionRowParams.topMargin = Dim.dp12;
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, Dim.dp36, 1.0f);
        leftParams.rightMargin = Dim.dp4;
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(0, Dim.dp36, 1.0f);
        rightParams.leftMargin = Dim.dp4;
        actionRow.addView(linkBtn, leftParams);
        actionRow.addView(cancelBtn, rightParams);
        content.addView(actionRow, actionRowParams);

        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = Dim.dp12;
        content.addView(status, statusParams);

        // CustomDialog layout order: [title, buttonContainer]. Insert custom content before the buttons.
        mainLayout.addView(content, mainLayout.getChildCount() - 1,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        dialog.show();
    }

    private static void showStatus(TextView status, CharSequence text, int color) {
        status.setText(text);
        status.setTextColor(color);
        status.setVisibility(View.VISIBLE);
    }
}
