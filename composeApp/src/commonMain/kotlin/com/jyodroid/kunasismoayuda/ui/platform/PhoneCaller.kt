package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable

/**
 * Places (or best-effort hands off) a phone call. Platform-specific on purpose: the common
 * `LocalUriHandler.openUri("tel:…")` mishandles two of our targets.
 * - **iOS:** a raw, human-typed number (spaces / dashes / parentheses) makes `NSURL(string:)`
 *   return `nil`, so the tap silently did nothing — the reported "call button doesn't work on iOS".
 *   The iOS actual sanitizes first, then opens the URL directly via `UIApplication`.
 * - **Desktop (JVM):** there is no dialer at all. The JVM actual tries a `tel:` hand-off (macOS
 *   FaceTime / a registered VoIP app) and otherwise copies the number to the clipboard.
 *
 * Android keeps working as before (its actual launches the system dialer via an `ACTION_DIAL` intent).
 */
fun interface PhoneCaller {
    /** Dial [rawNumber] (may contain spaces/dashes/parens; it's sanitized per platform). */
    fun call(rawNumber: String)
}

/** Remembers a [PhoneCaller] bound to the current platform (and, on Android, the local context). */
@Composable
expect fun rememberPhoneCaller(): PhoneCaller

/**
 * Reduce a human-entered number to what a dialer accepts: an optional leading `+`, digits, and the
 * DTMF keys `*`/`#`. Everything else — spaces, dashes, parentheses, letters (e.g. an IVR hint like
 * "opción 4") — is dropped. Returns `null` when nothing dialable remains, so callers can no-op.
 */
fun sanitizePhoneNumber(raw: String): String? {
    val out = StringBuilder()
    for (c in raw.trim()) {
        when {
            c.isDigit() -> out.append(c)
            c == '+' && out.isEmpty() -> out.append(c)
            c == '*' || c == '#' -> out.append(c)
        }
    }
    return out.toString().takeIf { s -> s.any(Char::isDigit) }
}
