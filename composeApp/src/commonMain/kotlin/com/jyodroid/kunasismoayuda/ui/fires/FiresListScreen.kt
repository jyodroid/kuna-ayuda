package com.jyodroid.kunasismoayuda.ui.fires

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.Fire
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.fire_detected
import com.jyodroid.kunasismoayuda.resources.overview_fire_none
import com.jyodroid.kunasismoayuda.ui.quakes.formatQuakeTime
import org.jetbrains.compose.resources.stringResource

/**
 * Browsable list of all active wildfires, most-relevant first (proximity-weighted intensity — see
 * [FiresViewModel.rankedFires]). Each row names the fire by nearest city + distance and its intensity;
 * tapping opens that fire's [FireDetailScreen]. This is how a user finds a fire near a specific place,
 * rather than only seeing the single headline fire on the Overview.
 */
@Composable
fun FiresListScreen(
    fires: List<Fire>,
    nearPlaceOf: (Fire) -> FirePlace?,
    onFireTap: (Fire) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (fires.isEmpty()) {
        Box(
            modifier.fillMaxSize().padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite },
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(Res.string.overview_fire_none), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(fires, key = { it.id }) { fire ->
            FireRow(fire = fire, near = nearPlaceOf(fire), onClick = { onFireTap(fire) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FireRow(fire: Fire, near: FirePlace?, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                firePlaceLabel(fire, near),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            FireIntensityBadge(fire.intensity)
            Text(
                stringResource(Res.string.fire_detected, formatQuakeTime(fire.timeMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
