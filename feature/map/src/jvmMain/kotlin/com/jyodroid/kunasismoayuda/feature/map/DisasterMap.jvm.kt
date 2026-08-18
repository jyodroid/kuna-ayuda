package com.jyodroid.kunasismoayuda.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

/**
 * Desktop placeholder. MapLibre's desktop runtime requires a Java 25 toolchain; enabling it is a
 * separate, deliberate task. As a stop-gap so desktop isn't a dead end, we open the location in the
 * system browser (OpenStreetMap) — centred on the focus point, or the first help point, whichever we
 * have. Tapping to drop a pin (the moderator location picker) still isn't possible here.
 */
@Composable
actual fun DisasterMap(
    markers: List<MapMarker>,
    onMarkerTap: (String) -> Unit,
    modifier: Modifier,
    focusLat: Double?,
    focusLon: Double?,
    focusZoom: Double?,
    userLat: Double?,
    userLon: Double?,
    onMapTap: ((Double, Double) -> Unit)?,
) {
    val target: Pair<Double, Double>? = when {
        focusLat != null && focusLon != null -> focusLat to focusLon
        markers.isNotEmpty() -> markers.first().latitude to markers.first().longitude
        else -> null
    }
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "El mapa interactivo está disponible en Android e iOS. En escritorio, usa la lista de refugios o abre la ubicación en el navegador.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            if (target != null) {
                val zoom = focusZoom?.toInt()?.coerceIn(1, 19) ?: 13
                Button(
                    onClick = { openInBrowser(target.first, target.second, zoom) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("Abrir el mapa en el navegador")
                }
            }
        }
    }
}

/** Opens OpenStreetMap centred on [lat],[lon] in the default browser (best-effort; ignored if unsupported). */
private fun openInBrowser(lat: Double, lon: Double, zoom: Int) {
    runCatching {
        val desktop = Desktop.getDesktop().takeIf { Desktop.isDesktopSupported() } ?: return
        if (!desktop.isSupported(Desktop.Action.BROWSE)) return
        desktop.browse(URI("https://www.openstreetmap.org/?mlat=$lat&mlon=$lon#map=$zoom/$lat/$lon"))
    }
}
