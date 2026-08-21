package com.jyodroid.kunasismoayuda.ui.platform

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** Android TTS via [TextToSpeech], shut down when it leaves the composition. */
@Composable
actual fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val speaker = remember(context) { AndroidSpeaker(context) }
    DisposableEffect(speaker) {
        onDispose { speaker.release() }
    }
    return speaker
}

private class AndroidSpeaker(context: Context) : Speaker {
    override val isAvailable: Boolean = true

    // TextToSpeech initializes asynchronously; speaking before SUCCESS is dropped by the engine, so
    // gate on this flag to avoid firing into a not-yet-ready engine.
    @Volatile
    private var ready = false

    private val engine = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) ready = true
    }

    override fun speak(text: String) {
        if (!ready || text.isBlank()) return
        // Match the device language (the tip text is already localized to it via Compose Resources).
        engine.language = Locale.getDefault()
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kuna-tip")
    }

    override fun stop() {
        engine.stop()
    }

    fun release() {
        engine.stop()
        engine.shutdown()
    }
}
