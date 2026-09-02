package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * Opens **Apple Maps** (the native maps app) at the coordinate via a `https://maps.apple.com/…`
 * universal link — the App-Store-required native option (Guideline 4). `ll` centers the map and `q`
 * with the same coordinate drops a pin there. We intentionally put only the coordinate in the URL (never
 * the free-text label), so the URL is always well-formed — a raw label with spaces/accents can make
 * `NSURL(string:)` return nil. https is a registered Maps universal link, so `canOpenURL` is true and no
 * `LSApplicationQueriesSchemes` entry is needed.
 */
@Composable
actual fun rememberMapLauncher(): MapLauncher = remember {
    MapLauncher { latitude, longitude, _ ->
        val point = "$latitude,$longitude"
        val url = NSURL(string = "https://maps.apple.com/?ll=$point&q=$point")
        val app = UIApplication.sharedApplication
        if (app.canOpenURL(url)) {
            app.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
    }
}
