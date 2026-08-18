package com.jyodroid.kunasismoayuda.feature.map

/**
 * A point plotted on [DisasterMap]. [kind] drives the marker colour so users can tell an epicentre
 * apart from the different kinds of aid center at a glance (never colour alone — callers pair the
 * map with a legend/list).
 *
 * @param id opaque id echoed back to `onMarkerTap` (e.g. "shelter:12" / "quake:us7000abcd")
 * @param label short name drawn beside the pin (site name); null → no on-map label
 */
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val kind: MarkerKind,
    val label: String? = null,
)

/** Marker categories with a fixed, high-contrast colour each (ARGB). */
enum class MarkerKind(val colorArgb: Long) {
    QUAKE(0xFFD32F2F),    // epicentre — red
    FIRE(0xFFE65100),     // active wildfire — deep orange
    ACOPIO(0xFF1565C0),   // collection point — blue
    ALBERGUE(0xFF2E7D32), // shelter — green
    SALUD(0xFFC2185B),    // medical — magenta
    AGUA(0xFF00838F),     // water — teal
    OTRO(0xFF616161),     // other — gray
}
