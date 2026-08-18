package com.jyodroid.kunasismoayuda.ui.fires

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.AffectedRegion
import com.jyodroid.kunasismoayuda.core.domain.model.Fire
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.fire_affected_title
import com.jyodroid.kunasismoayuda.resources.fire_confidence
import com.jyodroid.kunasismoayuda.resources.fire_detected
import com.jyodroid.kunasismoayuda.resources.fire_frp
import com.jyodroid.kunasismoayuda.resources.fire_source_note
import com.jyodroid.kunasismoayuda.resources.source
import com.jyodroid.kunasismoayuda.ui.quakes.formatQuakeTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun FireDetailScreen(
    fire: Fire,
    affectedRegions: List<AffectedRegion>,
    modifier: Modifier = Modifier,
    // FIRMS points have no place name; the nearest known city is used as a human label when present.
    nearPlace: FirePlace? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                firePlaceLabel(fire, nearPlace),
                style = MaterialTheme.typography.headlineSmall,
            )
            FireIntensityBadge(fire.intensity)
            Text(
                stringResource(Res.string.fire_detected, formatQuakeTime(fire.timeMillis)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider()

        fire.frpMw?.let { InfoRow(stringResource(Res.string.fire_frp, formatOneDecimal(it)), "") }
        fire.confidence?.let { InfoRow(stringResource(Res.string.fire_confidence, it), "") }
        InfoRow(stringResource(Res.string.source), fire.source)

        if (affectedRegions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(Res.string.fire_affected_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
            affectedRegions.take(6).forEach { ar ->
                InfoRow(
                    label = "${ar.region.name} (${ar.region.department})",
                    value = "${ar.distanceKm.toInt()} km",
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(Res.string.fire_source_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatOneDecimal(v: Double): String {
    val rounded = (v * 10).toLong()
    return "${rounded / 10}.${rounded % 10}"
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (value.isNotEmpty()) {
            Text(text = value, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
        }
    }
}
