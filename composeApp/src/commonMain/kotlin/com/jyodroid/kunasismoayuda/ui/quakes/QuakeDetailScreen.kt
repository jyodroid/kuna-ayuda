package com.jyodroid.kunasismoayuda.ui.quakes

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
import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.util.Geo
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.affected_regions
import com.jyodroid.kunasismoayuda.resources.aftershocks_empty
import com.jyodroid.kunasismoayuda.resources.aftershocks_title
import com.jyodroid.kunasismoayuda.resources.depth
import com.jyodroid.kunasismoayuda.resources.magnitude
import com.jyodroid.kunasismoayuda.resources.source
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuakeDetailScreen(
    quake: Quake,
    affectedRegions: List<AffectedRegion>,
    modifier: Modifier = Modifier,
    aftershocks: List<Quake> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MagnitudeBadge(quake.magnitude, size = 64)
            Column {
                Text(quake.place, style = MaterialTheme.typography.headlineSmall)
                Text(
                    formatQuakeTime(quake.timeMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider()

        InfoRow(stringResource(Res.string.magnitude), quake.magnitude?.let { formatMagnitude(it) } ?: "—")
        InfoRow(stringResource(Res.string.depth), quake.depthKm?.let { "${formatMagnitude(it)} km" } ?: "—")
        InfoRow(stringResource(Res.string.source), quake.source)

        if (affectedRegions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(Res.string.affected_regions),
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
            stringResource(Res.string.aftershocks_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        if (aftershocks.isEmpty()) {
            Text(
                stringResource(Res.string.aftershocks_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            aftershocks.take(10).forEach { after ->
                val distanceKm = Geo.distanceKm(
                    quake.latitude, quake.longitude, after.latitude, after.longitude,
                ).toInt()
                InfoRow(
                    label = "${after.magnitude?.let { "M${formatMagnitude(it)}" } ?: "—"} · ${after.place}",
                    value = "$distanceKm km · ${formatQuakeTime(after.timeMillis)}",
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Label takes the flexible space and wraps; the value stays on the right, so long place
        // names in aftershock rows never run off-screen.
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}
