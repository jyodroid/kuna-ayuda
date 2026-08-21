package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable

/**
 * Reads text aloud with the device's text-to-speech engine, in the device language — so a safety tip
 * is usable by someone who can't see well or can't read. Platform-specific (like [PhoneCaller]) because
 * each OS has its own TTS API: Android `TextToSpeech`, iOS `AVSpeechSynthesizer`. There is no desktop
 * TTS here, so the JVM actual reports [isAvailable] = false and the UI hides the "Listen" affordance.
 */
interface Speaker {
    /** False when the platform has no usable TTS (desktop) — callers should hide the Listen control. */
    val isAvailable: Boolean

    /** Speak [text] now, interrupting anything already being spoken. No-op when not [isAvailable]. */
    fun speak(text: String)

    /** Stop any in-progress speech. */
    fun stop()
}

/**
 * Remembers a [Speaker] bound to the current platform (and, on Android, the local context + a
 * `TextToSpeech` instance released when it leaves the composition).
 */
@Composable
expect fun rememberSpeaker(): Speaker
