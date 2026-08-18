package com.jyodroid.kunasismoayuda.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * An interactive map of disaster-relief points — aid/collection centers and the latest epicentre.
 * Backed by MapLibre on Android and iOS; on Desktop it shows a placeholder (MapLibre's desktop
 * runtime needs a Java 25 toolchain, deferred).
 *
 * Backed by a real street-level base map (OpenStreetMap vector tiles via OpenFreeMap) so streets,
 * neighbourhoods and place names around each point are visible; pins are labelled with the site name.
 *
 * @param markers points to plot; colour comes from [MapMarker.kind], the on-map label from [MapMarker.label]
 * @param onMarkerTap invoked with the tapped marker's [MapMarker.id]
 * @param focusLat/[focusLon] optional camera centre (e.g. the aid centers); Colombia if null
 * @param focusZoom optional zoom for the centre (higher = closer); a sensible default otherwise
 * @param userLat/[userLon] optional device location; when set, a distinct "you are here" dot is drawn
 * @param onMapTap optional; when non-null, tapping the map reports the tapped (lat, lon) — used by the
 *   moderator "add help point" flow to pick a location. Null (default) leaves map taps inert.
 */
@Composable
expect fun DisasterMap(
    markers: List<MapMarker>,
    onMarkerTap: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusLat: Double? = null,
    focusLon: Double? = null,
    focusZoom: Double? = null,
    userLat: Double? = null,
    userLon: Double? = null,
    onMapTap: ((Double, Double) -> Unit)? = null,
)
