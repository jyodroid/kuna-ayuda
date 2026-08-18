package com.jyodroid.kunasismoayuda.ui.fires

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.Fire
import com.jyodroid.kunasismoayuda.core.domain.model.FireIntensity
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.fire_intensity
import com.jyodroid.kunasismoayuda.resources.fire_intensity_high
import com.jyodroid.kunasismoayuda.resources.fire_intensity_low
import com.jyodroid.kunasismoayuda.resources.fire_intensity_moderate
import com.jyodroid.kunasismoayuda.resources.fire_place_km
import com.jyodroid.kunasismoayuda.resources.fire_place_near
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A human location label for a fire. Uses the fire's own [Fire.place] (GDACS carries one); else the
 * nearest known city — "Cerca de X" when close (≤40 km), "A N km de X" when farther (≤500 km); and if
 * even the nearest city is very far (remote wilderness), the coordinates so it's never "unknown".
 */
@Composable
fun firePlaceLabel(fire: Fire, near: FirePlace?): String {
    fire.place?.takeIf { it.isNotBlank() }?.let { return it }
    if (near != null) {
        return when {
            near.distanceKm <= 40 -> stringResource(Res.string.fire_place_near, near.name)
            near.distanceKm <= 500 -> stringResource(Res.string.fire_place_km, near.distanceKm.roundToInt().toString(), near.name)
            else -> fireCoords(fire.latitude, fire.longitude)
        }
    }
    return fireCoords(fire.latitude, fire.longitude)
}

/** Compact human-readable coordinates, e.g. "3.9°S, 67.5°W" (cardinal letters are language-neutral). */
private fun fireCoords(lat: Double, lon: Double): String {
    fun deg(value: Double, pos: String, neg: String): String {
        val whole = (abs(value) * 10).roundToInt()
        return "${whole / 10}.${whole % 10}°${if (value >= 0) pos else neg}"
    }
    return "${deg(lat, "N", "S")}, ${deg(lon, "E", "W")}"
}

/** Localized intensity word ("low"/"moderate"/"high") — always used so meaning is never colour-only. */
@Composable
fun fireIntensityWord(intensity: FireIntensity): String = stringResource(
    when (intensity) {
        FireIntensity.LOW -> Res.string.fire_intensity_low
        FireIntensity.MODERATE -> Res.string.fire_intensity_moderate
        FireIntensity.HIGH -> Res.string.fire_intensity_high
    },
)

/**
 * A pill naming the fire intensity in words plus a supporting colour (deep-orange family, escalating).
 * The colour reinforces; the word carries the meaning (accessibility: never colour alone). The badge
 * announces the full "Intensity: high" phrase to screen readers.
 */
@Composable
fun FireIntensityBadge(intensity: FireIntensity, modifier: Modifier = Modifier) {
    val word = fireIntensityWord(intensity)
    val full = stringResource(Res.string.fire_intensity, word)
    val bg = when (intensity) {
        FireIntensity.LOW -> Color(0xFFFFE0B2)
        FireIntensity.MODERATE -> Color(0xFFFFB74D)
        FireIntensity.HIGH -> Color(0xFFE65100)
    }
    val fg = if (intensity == FireIntensity.HIGH) Color.White else Color(0xFF3E2600)
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = full,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.heightIn(min = 24.dp).padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
