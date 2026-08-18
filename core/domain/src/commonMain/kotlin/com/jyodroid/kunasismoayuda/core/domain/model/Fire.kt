package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A normalized active-wildfire detection — the second hazard type alongside [Quake]. Time is epoch
 * milliseconds (UTC). [frpMw] (Fire Radiative Power, MW) is the primary intensity signal from FIRMS
 * points; [confidence] carries FIRMS' per-detection confidence ("low"/"nominal"/"high" or a 0–100
 * string) or the GDACS alert level. GDACS events have no [frpMw] and lean on [confidence]/[place].
 */
data class Fire(
    val id: String,
    val timeMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val brightnessK: Double?,
    val frpMw: Double?,
    val confidence: String?,
    val daynight: String?,
    val source: String,
    val place: String?,
) {
    /**
     * A coarse, source-agnostic intensity used for ranking and the severity label. FIRMS gives us FRP
     * (MW) — the honest signal; GDACS events (no FRP) fall back to their alert level. Higher = worse.
     */
    val intensity: FireIntensity
        get() = when {
            frpMw != null -> when {
                frpMw >= 100.0 -> FireIntensity.HIGH
                frpMw >= 20.0 -> FireIntensity.MODERATE
                else -> FireIntensity.LOW
            }
            else -> when (confidence?.lowercase()) {
                "red" -> FireIntensity.HIGH
                "orange" -> FireIntensity.MODERATE
                else -> FireIntensity.LOW
            }
        }
}

/** Coarse wildfire intensity bucket. The UI must name it in text, never convey it by colour alone. */
enum class FireIntensity { LOW, MODERATE, HIGH }
