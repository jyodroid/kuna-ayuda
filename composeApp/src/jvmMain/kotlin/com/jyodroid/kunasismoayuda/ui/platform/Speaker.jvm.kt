package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** Desktop has no bundled TTS engine, so the Listen control is hidden (`isAvailable = false`). */
@Composable
actual fun rememberSpeaker(): Speaker = remember { JvmSpeaker }

private object JvmSpeaker : Speaker {
    override val isAvailable: Boolean = false
    override fun speak(text: String) = Unit
    override fun stop() = Unit
}
