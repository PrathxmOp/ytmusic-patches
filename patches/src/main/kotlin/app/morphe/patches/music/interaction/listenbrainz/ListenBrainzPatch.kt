/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */
package app.morphe.patches.music.interaction.listenbrainz

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.music.misc.extension.sharedExtensionPatch
import app.morphe.patches.music.misc.settings.settingsPatch
import app.morphe.patches.music.shared.Constants.COMPATIBILITY_YOUTUBE_MUSIC
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

// ListenBrainz integration patch
@Suppress("unused")
val listenBrainzPatch = bytecodePatch(
    name = "ListenBrainz scrobbling",
    description = "Enables scrobbling listened tracks to ListenBrainz.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch
    )

    compatibleWith(COMPATIBILITY_YOUTUBE_MUSIC)

    execute {
        MediaSessionSetPlaybackStateFingerprint.let {
            it.method.apply {
                it.instructionMatches.reversed().forEach { match ->
                    val index = match.index
                    val register = getInstruction<FiveRegisterInstruction>(index).registerD
                    addInstruction(
                        index,
                        "invoke-static { v$register }, Lapp/morphe/extension/music/patches/listenbrainz/ListenBrainzHook;->onSetPlaybackState(Landroid/media/session/PlaybackState;)V"
                    )
                }
            }
        }

        MediaSessionSetMetadataFingerprint.let {
            it.method.apply {
                it.instructionMatches.reversed().forEach { match ->
                    val index = match.index
                    val register = getInstruction<FiveRegisterInstruction>(index).registerD
                    addInstruction(
                        index,
                        "invoke-static { v$register }, Lapp/morphe/extension/music/patches/listenbrainz/ListenBrainzHook;->onSetMetadata(Landroid/media/MediaMetadata;)V"
                    )
                }
            }
        }
    }
}
