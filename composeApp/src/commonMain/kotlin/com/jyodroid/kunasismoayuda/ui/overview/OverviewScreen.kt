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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.jyodroid.kunasismoayuda.core.domain.util.TimeAgo
import com.jyodroid.kunasismoayuda.core.domain.util.TimeAgoUnit
import com.jyodroid.kunasismoayuda.core.domain.util.isToday
import com.jyodroid.kunasismoayuda.core.domain.util.relativeAgo
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.country_change
import com.jyodroid.kunasismoayuda.resources.country_colombia
import com.jyodroid.kunasismoayuda.resources.country_indonesia
import com.jyodroid.kunasismoayuda.resources.country_italy
import com.jyodroid.kunasismoayuda.resources.country_peru
import com.jyodroid.kunasismoayuda.resources.country_spain
import com.jyodroid.kunasismoayuda.resources.data_window_fires
import com.jyodroid.kunasismoayuda.resources.error_generic
import com.jyodroid.kunasismoayuda.resources.loading
import com.jyodroid.kunasismoayuda.resources.no_quakes
import com.jyodroid.kunasismoayuda.resources.overview_affected_none
import com.jyodroid.kunasismoayuda.resources.overview_affected_subtitle
import com.jyodroid.kunasismoayuda.resources.overview_affected_title
import com.jyodroid.kunasismoayuda.resources.overview_chip_board
import com.jyodroid.kunasismoayuda.resources.overview_chip_fires
import com.jyodroid.kunasismoayuda.resources.overview_chip_quakes
import com.jyodroid.kunasismoayuda.resources.overview_chip_shelters
import com.jyodroid.kunasismoayuda.resources.overview_fire_count
import com.jyodroid.kunasismoayuda.resources.overview_fire_hint
import com.jyodroid.kunasismoayuda.resources.overview_fire_none
import com.jyodroid.kunasismoayuda.resources.overview_fire_title
import com.jyodroid.kunasismoayuda.resources.overview_guide_cta
import com.jyodroid.kunasismoayuda.resources.overview_nearest_shelter
import com.jyodroid.kunasismoayuda.resources.overview_network_counts
import com.jyodroid.kunasismoayuda.resources.overview_network_title
import com.jyodroid.kunasismoayuda.resources.overview_quake_hint
import com.jyodroid.kunasismoayuda.resources.overview_quake_today
import com.jyodroid.kunasismoayuda.resources.overview_quake_window
import com.jyodroid.kunasismoayuda.resources.overview_recent_quake_title
import com.jyodroid.kunasismoayuda.resources.overview_recent_quake_window
import com.jyodroid.kunasismoayuda.resources.overview_shelters_count_short
import com.jyodroid.kunasismoayuda.resources.overview_shelters_title
import com.jyodroid.kunasismoayuda.resources.overview_status_calm
import com.jyodroid.kunasismoayuda.resources.overview_status_calm_sub
import com.jyodroid.kunasismoayuda.resources.overview_updated
import com.jyodroid.kunasismoayuda.resources.overview_use_location
import com.jyodroid.kunasismoayuda.resources.retry
import com.jyodroid.kunasismoayuda.resources.shelter_distance
import com.jyodroid.kunasismoayuda.resources.sos_safe_button
import com.jyodroid.kunasismoayuda.resources.time_days_ago
import com.jyodroid.kunasismoayuda.resources.time_hours_ago
import com.jyodroid.kunasismoayuda.resources.time_just_now
import com.jyodroid.kunasismoayuda.resources.time_minutes_ago
import com.jyodroid.kunasismoayuda.ui.board.BoardSummary
import com.jyodroid.kunasismoayuda.ui.fires.FirePlace
import com.jyodroid.kunasismoayuda.ui.fires.FireIntensityBadge
import com.jyodroid.kunasismoayuda.ui.fires.firePlaceLabel
import com.jyodroid.kunasismoayuda.ui.quakes.MagnitudeBadge
import com.jyodroid.kunasismoayuda.ui.quakes.QuakesUiState
import com.jyodroid.kunasismoayuda.ui.quakes.formatQuakeTime
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Home/summary tab. A **StatusHeader** reads the whole situation at a glance (count chips, a calm
 * "no active alerts" state, a freshness stamp, and a quick "I'm safe" shortcut); below it come the help
 * summaries and the hazard bubbles. Each hazard figure is labelled with its window so it's clear what it
 * means: the quake bubble is *the strongest of the last 30 days*, the fire bubble *the most relevant of N
 * active fires in the last 48 h*. The quake/fire are **bubbles** — tapping opens the full detail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    state: QuakesUiState,
    featuredQuake: Quake?,
    recentQuake: Quake?,
    nowMillis: Long,
    affectedRegions: List<AffectedRegion>,
    shelters: List<Shelter>,
    boardSummary: BoardSummary,
    featuredFire: Fire?,
    featuredFireNear: FirePlace? = null,
    // Situation counts for the header chips + the honest window labels.
    quakeCount: Int = 0,
    fireCount: Int = 0,
    // When a load last landed (null = not yet); shown as "Actualizado · hace N min".
    lastUpdatedMillis: Long? = null,
    // The closest help point to the user, once location is granted (name + km). Null = no location yet.
    nearestShelter: Pair<Shelter, Double>? = null,
    currentCountry: Country,
    onCountryChange: (Country) -> Unit,
    onRefresh: () -> Unit,
    onQuakeTap: () -> Unit,
    onRecentQuakeTap: () -> Unit,
    onFireTap: () -> Unit,
    onSheltersTap: () -> Unit,
    onNetworkTap: () -> Unit,
    onUseLocation: () -> Unit = {},
    onGuideTap: () -> Unit = {},
    onSafeCheckIn: () -> Unit = {},
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
                        // At-a-glance situation first, then help (shelters, aid network, affected
                        // places), then the hazard bubbles → detail + réplicas.
                        item {
                            StatusHeader(
                                quakeCount = quakeCount,
                                fireCount = fireCount,
                                sheltersCount = shelters.size,
                                boardCount = boardSummary.offers + boardSummary.requests,
                                hasActiveHazard = featuredQuake != null || recentQuake != null || fireCount > 0,
                                lastUpdatedMillis = lastUpdatedMillis,
                                nowMillis = nowMillis,
                                onQuakeTap = onQuakeTap,
                                onFireTap = onFireTap,
                                onSheltersTap = onSheltersTap,
                                onNetworkTap = onNetworkTap,
                                onGuideTap = onGuideTap,
                                onSafeCheckIn = onSafeCheckIn,
                            )
                        }
                        item { SheltersSummary(shelters, nearestShelter, onSheltersTap, onUseLocation) }
                        item { NetworkSummary(boardSummary, onNetworkTap) }
                        item { AffectedPlaces(affectedRegions) }
                        item { QuakeBubble(featuredQuake, nowMillis, onQuakeTap) }
                        // A fresh quake weaker than the headline would otherwise be invisible; surface
                        // it here (only when it differs from the strongest — see mostRecentQuake).
                        if (recentQuake != null) {
                            item { RecentQuakeBubble(recentQuake, nowMillis, onRecentQuakeTap) }
                        }
                        item { FireBubble(featuredFire, featuredFireNear, fireCount, onFireTap) }
                        item { AppVersionFooter() }
                    }
                }
            }
        }
    }
}

/**
 * The one-glance situation banner: a freshness stamp + a quick "I'm safe" shortcut, an optional calm
 * "no active alerts" card when nothing is happening, and a scrollable row of tappable count chips.
 */
@Composable
private fun StatusHeader(
    quakeCount: Int,
    fireCount: Int,
    sheltersCount: Int,
    boardCount: Int,
    hasActiveHazard: Boolean,
    lastUpdatedMillis: Long?,
    nowMillis: Long,
    onQuakeTap: () -> Unit,
    onFireTap: () -> Unit,
    onSheltersTap: () -> Unit,
    onNetworkTap: () -> Unit,
    onGuideTap: () -> Unit,
    onSafeCheckIn: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (lastUpdatedMillis != null) {
                Text(
                    stringResource(
                        Res.string.overview_updated,
                        relativeAgoText(relativeAgo(nowMillis, lastUpdatedMillis)),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            TextButton(onClick = onSafeCheckIn, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(Res.string.sos_safe_button))
            }
        }

        // When nothing is active, reassure and point to preparedness rather than showing empty bubbles.
        if (!hasActiveHazard) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(Res.string.overview_status_calm),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        stringResource(Res.string.overview_status_calm_sub),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = onGuideTap, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(Res.string.overview_guide_cta))
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CountChip(quakeCount, stringResource(Res.string.overview_chip_quakes), onQuakeTap)
            CountChip(fireCount, stringResource(Res.string.overview_chip_fires), onFireTap)
            CountChip(sheltersCount, stringResource(Res.string.overview_chip_shelters), onSheltersTap, enabledWhenZero = true)
            CountChip(boardCount, stringResource(Res.string.overview_chip_board), onNetworkTap, enabledWhenZero = true)
        }
    }
}

/** A compact "N Label" chip. Disabled (greyed) when the count is 0 and tapping wouldn't lead anywhere. */
@Composable
private fun CountChip(count: Int, label: String, onClick: () -> Unit, enabledWhenZero: Boolean = false) {
    AssistChip(
        onClick = onClick,
        enabled = count > 0 || enabledWhenZero,
        label = { Text("$count $label") },
        colors = AssistChipDefaults.assistChipColors(),
    )
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
    Country.PERU -> Res.string.country_peru
}

@Composable
private fun QuakeBubble(quake: Quake?, nowMillis: Long, onTap: () -> Unit) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            quake.place,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        // A "Hoy" badge when the strongest event is itself fresh (< 24h), so a quake
                        // that headlines today still reads as today despite the absolute timestamp.
                        if (isToday(nowMillis, quake.timeMillis)) TodayBadge()
                    }
                    // The window label: this is the strongest of the last 30 days, not "the" quake.
                    Text(
                        stringResource(Res.string.overview_quake_window),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

/**
 * The most-recent-quake bubble: highlights fresh seismic activity that isn't the headline (which is the
 * strongest, not the newest). Uses **relative** time ("hace N h") — freshness reads naturally that way,
 * whereas the featured bubble keeps an absolute date for a possibly-historical "most relevant" event.
 */
@Composable
private fun RecentQuakeBubble(quake: Quake, nowMillis: Long, onTap: () -> Unit) {
    val hint = stringResource(Res.string.overview_quake_hint)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onTap),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(Res.string.overview_recent_quake_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    stringResource(Res.string.overview_recent_quake_window),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MagnitudeBadge(quake.magnitude, size = 56)
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            quake.place,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (isToday(nowMillis, quake.timeMillis)) TodayBadge()
                    }
                    Text(
                        relativeAgoText(relativeAgo(nowMillis, quake.timeMillis)),
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

/** Small "Hoy" chip. Freshness is stated in text (never colour alone); not red — red is reserved for SOS. */
@Composable
private fun TodayBadge() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            stringResource(Res.string.overview_quake_today),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun relativeAgoText(ago: TimeAgo): String = when (ago.unit) {
    TimeAgoUnit.JUST_NOW -> stringResource(Res.string.time_just_now)
    TimeAgoUnit.MINUTES -> stringResource(Res.string.time_minutes_ago, ago.count)
    TimeAgoUnit.HOURS -> stringResource(Res.string.time_hours_ago, ago.count)
    TimeAgoUnit.DAYS -> stringResource(Res.string.time_days_ago, ago.count)
}

@Composable
private fun FireBubble(fire: Fire?, near: FirePlace?, fireCount: Int, onTap: () -> Unit) {
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
                    // Count + window, so it's clear this is the most relevant of N active fires.
                    Text(
                        "${stringResource(Res.string.overview_fire_count, fireCount)} · ${stringResource(Res.string.data_window_fires)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/**
 * A short help-centers summary: a single count + a quick link (the per-city breakdown lived here before,
 * but the fragile address-tail parsing made it noisy and it duplicated the Refugios screen). When the
 * user has granted location, it also names the nearest help point; otherwise it offers to find it.
 */
@Composable
private fun SheltersSummary(
    shelters: List<Shelter>,
    nearest: Pair<Shelter, Double>?,
    onClick: () -> Unit,
    onUseLocation: () -> Unit,
) {
    SummaryCard(
        title = stringResource(Res.string.overview_shelters_title),
        subtitle = stringResource(Res.string.overview_shelters_count_short, shelters.size),
        onClick = onClick,
    ) {
        if (nearest != null) {
            val (s, km) = nearest
            val shown = ((km * 10).roundToInt() / 10.0).toString()
            Text(
                "${stringResource(Res.string.overview_nearest_shelter)}: ${s.name} · ${stringResource(Res.string.shelter_distance, shown)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        } else if (shelters.isNotEmpty()) {
            TextButton(onClick = onUseLocation, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(Res.string.overview_use_location))
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
    SummaryCard(
        title = stringResource(Res.string.overview_affected_title),
        // Clarify what these places relate to and what the distance means (from the quake epicenter).
        subtitle = if (affected.isEmpty()) null else stringResource(Res.string.overview_affected_subtitle),
    ) {
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
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}
