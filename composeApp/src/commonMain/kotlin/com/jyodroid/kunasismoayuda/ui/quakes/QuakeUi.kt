package com.jyodroid.kunasismoayuda.ui.quakes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.magnitude
import com.jyodroid.kunasismoayuda.resources.severity_light
import com.jyodroid.kunasismoayuda.resources.severity_moderate
import com.jyodroid.kunasismoayuda.resources.severity_strong
import com.jyodroid.kunasismoayuda.resources.severity_unknown
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Color-codes a magnitude on a green → amber → orange → red scale. */
fun magnitudeColor(magnitude: Double?): Color = when {
    magnitude == null -> Color(0xFF616161)
    magnitude < 3.0 -> Color(0xFF2E7D32) // green
    magnitude < 4.5 -> Color(0xFFF9A825) // amber
    magnitude < 6.0 -> Color(0xFFEF6C00) // orange
    else -> Color(0xFFC62828)            // red
}

/** Text color chosen for contrast against the badge color (amber is light → use black). */
private fun onMagnitudeColor(magnitude: Double?): Color =
    if (magnitude != null && magnitude >= 3.0 && magnitude < 4.5) Color.Black else Color.White

/** A non-color severity word, so severity isn't conveyed by color alone. */
private fun severityRes(magnitude: Double?): StringResource = when {
    magnitude == null -> Res.string.severity_unknown
    magnitude < 3.0 -> Res.string.severity_light
    magnitude < 4.5 -> Res.string.severity_moderate
    else -> Res.string.severity_strong
}

@Composable
fun MagnitudeBadge(magnitude: Double?, size: Int = 48) {
    val label = magnitude?.let { formatMagnitude(it) } ?: "—"
    // Screen readers announce e.g. "Magnitud 5.0, fuerte" instead of just a colored circle.
    val description = "${stringResource(Res.string.magnitude)} $label, ${stringResource(severityRes(magnitude))}"
    Surface(
        color = magnitudeColor(magnitude),
        shape = CircleShape,
        modifier = Modifier
            .size(size.dp)
            .semantics(mergeDescendants = true) { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = onMagnitudeColor(magnitude),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** One decimal place, no locale dependency. */
fun formatMagnitude(mag: Double): String {
    val rounded = (mag * 10).toLong()
    val whole = rounded / 10
    val frac = (if (rounded < 0) -rounded else rounded) % 10
    return "$whole.$frac"
}
