package com.jyodroid.kunasismoayuda.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

/**
 * Desktop has no maps app; open the coordinate on OpenStreetMap in the browser (same URL the desktop
 * map's "open in browser" button uses). The pin [label] isn't needed — the marker sits at the coords.
 */
@Composable
actual fun rememberMapLauncher(): MapLauncher = remember {
    MapLauncher { latitude, longitude, _ ->
        runCatching {
            val desktop = Desktop.getDesktop().takeIf { Desktop.isDesktopSupported() }
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(
                    URI("https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=16/$latitude/$longitude"),
                )
            }
        }
    }
}
