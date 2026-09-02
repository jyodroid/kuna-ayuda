package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable

/**
 * Opens a coordinate in the device's **native** maps app. Platform-specific on purpose (App Store
 * Guideline 4 requires the native Apple Maps option, not a third-party maps app):
 * - **iOS:** launches **Apple Maps** via a `https://maps.apple.com/…` universal link.
 * - **Android:** hands off to the user's default maps app via a `geo:` intent (chooser if several).
 * - **Desktop (JVM):** opens OpenStreetMap in the browser (no maps app on desktop).
 */
fun interface MapLauncher {
    /** Show [latitude],[longitude] on a map, with an optional [label] as the pin title. */
    fun open(latitude: Double, longitude: Double, label: String?)
}

/** Remembers a [MapLauncher] bound to the current platform (and, on Android, the local context). */
@Composable
expect fun rememberMapLauncher(): MapLauncher
