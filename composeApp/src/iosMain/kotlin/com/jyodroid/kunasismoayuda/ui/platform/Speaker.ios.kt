package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFAudio.AVSpeechBoundary.AVSpeechBoundaryImmediate
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance

/** iOS TTS via [AVSpeechSynthesizer], voiced in the device's current speech language. */
@Composable
actual fun rememberSpeaker(): Speaker = remember { IosSpeaker() }

private class IosSpeaker : Speaker {
    override val isAvailable: Boolean = true

    private val synthesizer = AVSpeechSynthesizer()

    override fun speak(text: String) {
        if (text.isBlank()) return
        if (synthesizer.isSpeaking()) synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        // Match the device's speech language so the (already-localized) tip is read naturally.
        AVSpeechSynthesisVoice.voiceWithLanguage(AVSpeechSynthesisVoice.currentLanguageCode())
            ?.let { utterance.voice = it }
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
    }
}
