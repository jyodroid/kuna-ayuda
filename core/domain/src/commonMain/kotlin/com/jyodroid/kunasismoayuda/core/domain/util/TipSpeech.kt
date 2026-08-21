package com.jyodroid.kunasismoayuda.core.domain.util

/**
 * Builds the phrase spoken aloud for a safety tip: the title, then the body, so a low-vision or
 * non-reading user hears the same content the card shows. Pure so it's unit-testable; the platform
 * text-to-speech lives in the app (`ui/platform/Speaker`).
 *
 * The title gets a trailing period (when it doesn't already end in sentence punctuation) so speech
 * engines pause between the heading and the detail instead of running them together.
 */
fun tipSpeechText(title: String, body: String): String {
    val t = title.trim()
    val b = body.trim()
    if (t.isEmpty()) return b
    if (b.isEmpty()) return t
    val separator = if (t.last() in ".!?:") " " else ". "
    return t + separator + b
}
