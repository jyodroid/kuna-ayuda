package com.jyodroid.kunasismoayuda.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.Anchor
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

private const val MARKER_ID_PROPERTY = "markerId"
private const val LABEL_PROPERTY = "label"

// OpenFreeMap "Liberty" — free, no API key, OSM vector tiles with full street/place labels. This is
// what makes streets and neighbourhoods visible around each help point. (Community-hosted; the tiles
// can be self-hosted later for guaranteed uptime / offline.)
private const val BASE_STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"

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
    val hasFocus = focusLat != null && focusLon != null
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            // Centre on the aid centers when known, otherwise fit Colombia.
            target = Position(longitude = focusLon ?: -74.0, latitude = focusLat ?: 4.6),
            zoom = focusZoom ?: if (hasFocus) 7.0 else 4.5,
        ),
    )

    // Recentre whenever the focus changes (e.g. toggling "near me") — firstPosition only seeds the
    // very first frame, so without this the camera would never follow later focus updates.
    LaunchedEffect(focusLat, focusLon, focusZoom) {
        if (focusLat != null && focusLon != null) {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(longitude = focusLon, latitude = focusLat),
                    zoom = focusZoom ?: cameraState.position.zoom,
                ),
            )
        }
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(BASE_STYLE_URI),
        cameraState = cameraState,
        // In "pick a location" mode, a map tap reports its coordinates and is consumed; otherwise inert.
        onMapClick = { position, _ ->
            if (onMapTap != null) {
                onMapTap(position.latitude, position.longitude)
                ClickResult.Consume
            } else {
                ClickResult.Pass
            }
        },
    ) {
        // Anchor.Top keeps our markers in front of *every* base-map layer — on a full street style the
        // help-point dots must never get lost behind streets/POIs/labels.
        Anchor.Top {
            // One circle + one label layer per kind (fixed count → stable composition). Colour
            // distinguishes kinds; a bold white-ringed dot keeps them visible over a busy street map.
            MarkerKind.entries.forEach { kind ->
                val features = markers.asSequence()
                    .filter { it.kind == kind }
                    .map { marker ->
                        Feature(
                            geometry = Point(Position(marker.longitude, marker.latitude)),
                            properties = JsonObject(
                                buildMap {
                                    put(MARKER_ID_PROPERTY, JsonPrimitive(marker.id))
                                    marker.label?.let { put(LABEL_PROPERTY, JsonPrimitive(it)) }
                                },
                            ),
                        )
                    }
                    .toList()
                    .toTypedArray()

                val source = rememberGeoJsonSource(
                    data = GeoJsonData.Features(FeatureCollection(*features)),
                )
                CircleLayer(
                    id = "markers-${kind.name}",
                    source = source,
                    color = const(Color(kind.colorArgb)),
                    radius = const(10.dp),
                    strokeColor = const(Color.White),
                    strokeWidth = const(3.dp),
                    onClick = { clicked ->
                        val id = clicked.firstOrNull()
                            ?.properties?.get(MARKER_ID_PROPERTY)?.jsonPrimitive?.content
                        if (id != null) {
                            onMarkerTap(id)
                            ClickResult.Consume
                        } else {
                            ClickResult.Pass
                        }
                    },
                )
                // Site-name labels only from zoom 9+ (city level) so the overview stays uncluttered dots.
                SymbolLayer(
                    id = "labels-${kind.name}",
                    source = source,
                    minZoom = 9f,
                    textField = format(span(feature[LABEL_PROPERTY].asString())),
                    textColor = const(Color(0xFF1A1A1A)),
                    textHaloColor = const(Color.White),
                    textHaloWidth = const(1.5.dp),
                    textSize = const(0.75.em),
                    textAnchor = const(SymbolAnchor.Top),
                    textOffset = offset(0f.em, 0.9f.em),
                )
            }

            // "You are here" — a distinct blue dot with a white ring, only when a device location is known.
            if (userLat != null && userLon != null) {
                val userSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        FeatureCollection(
                            Feature(
                                geometry = Point(Position(userLon, userLat)),
                                properties = JsonObject(emptyMap()),
                            ),
                        ),
                    ),
                )
                CircleLayer(
                    id = "user-location",
                    source = userSource,
                    color = const(Color(0xFF1E88E5)),
                    radius = const(9.dp),
                    strokeColor = const(Color.White),
                    strokeWidth = const(3.dp),
                )
            }
        }
    }
}
