package com.jyodroid.kunasismoayuda.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.AffectedRegion
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.Fire
import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.model.Shelter
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.country_change
import com.jyodroid.kunasismoayuda.resources.country_colombia
import com.jyodroid.kunasismoayuda.resources.country_indonesia
import com.jyodroid.kunasismoayuda.resources.country_italy
import com.jyodroid.kunasismoayuda.resources.country_spain
import com.jyodroid.kunasismoayuda.resources.error_generic
import com.jyodroid.kunasismoayuda.resources.loading
import com.jyodroid.kunasismoayuda.resources.no_quakes
import com.jyodroid.kunasismoayuda.resources.overview_affected_none
import com.jyodroid.kunasismoayuda.resources.overview_affected_title
import com.jyodroid.kunasismoayuda.resources.overview_fire_hint
import com.jyodroid.kunasismoayuda.resources.overview_fire_none
import com.jyodroid.kunasismoayuda.resources.overview_fire_title
import com.jyodroid.kunasismoayuda.resources.overview_network_counts
import com.jyodroid.kunasismoayuda.resources.overview_network_title
import com.jyodroid.kunasismoayuda.resources.overview_quake_hint
import com.jyodroid.kunasismoayuda.resources.overview_shelters_count
import com.jyodroid.kunasismoayuda.resources.overview_shelters_title
import com.jyodroid.kunasismoayuda.resources.retry
import com.jyodroid.kunasismoayuda.ui.board.BoardSummary
import com.jyodroid.kunasismoayuda.ui.fires.FirePlace
import com.jyodroid.kunasismoayuda.ui.fires.FireIntensityBadge
import com.jyodroid.kunasismoayuda.ui.fires.firePlaceLabel
import com.jyodroid.kunasismoayuda.ui.quakes.MagnitudeBadge
import com.jyodroid.kunasismoayuda.ui.quakes.QuakesUiState
import com.jyodroid.kunasismoayuda.ui.quakes.formatQuakeTime
import org.jetbrains.compose.resources.stringResource

/**
 * Home/summary tab: one glanceable card per topic. The quake is a **bubble** — tapping it opens the
 * full detail + réplicas ([onQuakeTap]); the map tab never shows quake info. Shelters are summarized
 * per location with the quake's affected places first, so aid near the affected area stands out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    state: QuakesUiState,
    featuredQuake: Quake?,
    affectedRegions: List<AffectedRegion>,
    shelters: List<Shelter>,
    boardSummary: BoardSummary,
    featuredFire: Fire?,
    featuredFireNear: FirePlace? = null,
    currentCountry: Country,
    onCountryChange: (Country) -> Unit,
    onRefresh: () -> Unit,
    onQuakeTap: () -> Unit,
    onFireTap: () -> Unit,
    onSheltersTap: () -> Unit,
    onNetworkTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        // The country switcher is always visible (even while loading), so a country whose feed is
        // empty or failing can still be switched away from.
        CountrySelector(
            current = currentCountry,
            onChange = onCountryChange,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )
        // Pull down to retry — the summary loads from the network, so a transient failure (or the
        // server not yet reachable at launch) is recoverable without restarting the app.
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    // Loading with nothing yet: the pull spinner already communicates progress.
                    state.isLoading && featuredQuake == null && shelters.isEmpty() -> item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(Res.string.loading))
                        }
                    }

                    state.error && featuredQuake == null -> item {
                        Box(
                            modifier = Modifier.fillParentMaxSize()
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(Res.string.error_generic))
                                Button(onClick = onRefresh, modifier = Modifier.padding(top = 12.dp)) {
                                    Text(stringResource(Res.string.retry))
                                }
                            }
                        }
                    }

                    else -> {
                        // Help first (shelters, aid network, affected places); the quake is
                        // de-prioritized to the bottom as a tappable bubble → detail + réplicas.
                        item { SheltersSummary(shelters, affectedRegions, onSheltersTap) }
                        item { NetworkSummary(boardSummary, onNetworkTap) }
                        item { AffectedPlaces(affectedRegions) }
                        item { QuakeBubble(featuredQuake, onQuakeTap) }
                        item { FireBubble(featuredFire, featuredFireNear, onFireTap) }
                        item { AppVersionFooter() }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppVersionFooter() {
    Text(
        text = "Kuna Ayuda · v${com.jyodroid.kunasismoayuda.core.domain.AppInfo.VERSION}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
    )
}

@Composable
private fun CountrySelector(
    current: Country,
    onChange: (Country) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier.fillMaxWidth().clickable { expanded = true }) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(current.flag, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(current.labelRes()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                stringResource(Res.string.country_change),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Country.entries.forEach { c ->
                DropdownMenuItem(
                    text = { Text("${c.flag}  ${stringResource(c.labelRes())}") },
                    onClick = {
                        expanded = false
                        onChange(c)
                    },
                )
            }
        }
    }
}

private fun Country.labelRes() = when (this) {
    Country.COLOMBIA -> Res.string.country_colombia
    Country.INDONESIA -> Res.string.country_indonesia
    Country.SPAIN -> Res.string.country_spain
    Country.ITALY -> Res.string.country_italy
}

@Composable
private fun QuakeBubble(quake: Quake?, onTap: () -> Unit) {
    val hint = stringResource(Res.string.overview_quake_hint)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .let { if (quake != null) it.clickable(onClick = onTap) else it },
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (quake == null) {
                Text(stringResource(Res.string.no_quakes), style = MaterialTheme.typography.bodyLarge)
            } else {
                MagnitudeBadge(quake.magnitude, size = 56)
                Column {
                    Text(quake.place, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        formatQuakeTime(quake.timeMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        hint,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun FireBubble(fire: Fire?, near: FirePlace?, onTap: () -> Unit) {
    val hint = stringResource(Res.string.overview_fire_hint)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .let { if (fire != null) it.clickable(onClick = onTap) else it },
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (fire == null) {
                Text(stringResource(Res.string.overview_fire_none), style = MaterialTheme.typography.bodyLarge)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(Res.string.overview_fire_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FireIntensityBadge(fire.intensity)
                    Text(
                        firePlaceLabel(fire, near),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(hint, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun SheltersSummary(
    shelters: List<Shelter>,
    affected: List<AffectedRegion>,
    onClick: () -> Unit,
) {
    val byLocation = sheltersByLocation(shelters, affected)
    SummaryCard(
        title = stringResource(Res.string.overview_shelters_title),
        subtitle = stringResource(Res.string.overview_shelters_count, shelters.size, byLocation.size),
        onClick = onClick,
    ) {
        byLocation.forEach { (city, count) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(city, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$count", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun NetworkSummary(summary: BoardSummary, onClick: () -> Unit) {
    SummaryCard(
        title = stringResource(Res.string.overview_network_title),
        subtitle = stringResource(Res.string.overview_network_counts, summary.offers, summary.requests),
        onClick = onClick,
    ) {}
}

@Composable
private fun AffectedPlaces(affected: List<AffectedRegion>) {
    SummaryCard(title = stringResource(Res.string.overview_affected_title), subtitle = null) {
        if (affected.isEmpty()) {
            Text(
                stringResource(Res.string.overview_affected_none),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            affected.take(8).forEach { ar ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${ar.region.name} (${ar.region.department})", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${ar.distanceKm.toInt()} km", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    Card(cardModifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            content()
        }
    }
}

@Composable
private fun Centered(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

/**
 * Groups shelters by city (parsed from the address tail after the last comma; "Otros" if none),
 * counting each. Cities that are among the quake's affected regions are listed first so aid near the
 * affected area is what the user sees, ahead of unaffected big cities.
 */
internal fun sheltersByLocation(
    shelters: List<Shelter>,
    affected: List<AffectedRegion>,
): List<Pair<String, Int>> {
    val affectedNames = affected.map { it.region.name }.toSet()
    val counts = shelters
        .groupingBy { it.address.substringAfterLast(',', "").trim().ifBlank { "Otros" } }
        .eachCount()
    return counts.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.key in affectedNames }
                .thenByDescending { it.value }
                .thenBy { it.key },
        )
        .map { it.key to it.value }
}
