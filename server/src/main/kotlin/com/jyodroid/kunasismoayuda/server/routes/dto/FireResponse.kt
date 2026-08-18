package com.jyodroid.kunasismoayuda.server.routes.dto

import kotlinx.serialization.Serializable

/**
 * Normalized active-wildfire shape the KMP client consumes, independent of whichever upstream
 * (NASA FIRMS thermal-anomaly points or GDACS wildfire events) produced it — the parallel of
 * [QuakeResponse] for the second hazard vertical.
 *
 * [frpMw] (Fire Radiative Power, megawatts) is the primary severity signal for FIRMS points;
 * [confidence] carries FIRMS' per-detection confidence (`low`/`nominal`/`high` for VIIRS, or a
 * 0–100 string for MODIS). GDACS events fill [frpMw]=null and lean on [confidence]/[place] instead.
 */
@Serializable
data class FireResponse(
    val id: String,
    val time: Long,             // epoch millis (UTC) of acquisition / event date
    val latitude: Double,
    val longitude: Double,
    val brightnessK: Double?,   // brightness temperature (Kelvin), FIRMS bright_ti4/MODIS brightness
    val frpMw: Double?,         // Fire Radiative Power (MW) — higher = more intense
    val confidence: String?,    // "low"/"nominal"/"high" (VIIRS) or "0".."100" (MODIS); GDACS alert level
    val daynight: String?,      // "D" / "N" (FIRMS); null for GDACS
    val source: String,         // e.g. "FIRMS (VIIRS_SNPP_NRT)" or "GDACS"
    val place: String? = null,  // human place label (GDACS); FIRMS points have none
)
