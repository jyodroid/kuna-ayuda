package com.jyodroid.kunasismoayuda.ui.shelters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jyodroid.kunasismoayuda.ui.platform.rememberMapLauncher
import com.jyodroid.kunasismoayuda.ui.platform.rememberPhoneCaller
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.Shelter
import com.jyodroid.kunasismoayuda.core.domain.model.ShelterType
import com.jyodroid.kunasismoayuda.core.domain.util.Geo
import androidx.compose.material3.Icon
import org.jetbrains.compose.resources.painterResource
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.ic_check
import com.jyodroid.kunasismoayuda.resources.admin_cancel
import com.jyodroid.kunasismoayuda.resources.help_call
import com.jyodroid.kunasismoayuda.resources.map_locating
import com.jyodroid.kunasismoayuda.resources.map_near_me
import com.jyodroid.kunasismoayuda.resources.map_show_all
import com.jyodroid.kunasismoayuda.resources.retry
import com.jyodroid.kunasismoayuda.resources.shelter_delete
import com.jyodroid.kunasismoayuda.resources.shelter_delete_confirm_msg
import com.jyodroid.kunasismoayuda.resources.shelter_delete_confirm_title
import com.jyodroid.kunasismoayuda.resources.shelter_directions
import com.jyodroid.kunasismoayuda.resources.shelter_edit
import com.jyodroid.kunasismoayuda.resources.shelter_distance
import com.jyodroid.kunasismoayuda.resources.shelter_type_acopio
import com.jyodroid.kunasismoayuda.resources.shelter_type_agua
import com.jyodroid.kunasismoayuda.resources.shelter_type_albergue
import com.jyodroid.kunasismoayuda.resources.shelter_type_otro
import com.jyodroid.kunasismoayuda.resources.shelter_type_salud
import com.jyodroid.kunasismoayuda.resources.shelters_accepts
import com.jyodroid.kunasismoayuda.resources.shelters_empty
import com.jyodroid.kunasismoayuda.resources.shelters_error
import com.jyodroid.kunasismoayuda.resources.shelters_filter_all
import com.jyodroid.kunasismoayuda.resources.shelters_hours
import com.jyodroid.kunasismoayuda.resources.shelters_last_verified
import com.jyodroid.kunasismoayuda.resources.shelters_loading
import com.jyodroid.kunasismoayuda.resources.shelters_official_note
import com.jyodroid.kunasismoayuda.resources.shelters_verified
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun SheltersScreen(
    state: SheltersUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    // Moderator-only: when true, each card gets Edit/Delete affordances.
    isModerator: Boolean = false,
    onEditShelter: (Shelter) -> Unit = {},
    onDeleteShelter: (Shelter) -> Unit = {},
    // Revertible "near me": device coords + toggle fed from App.kt (shared location machinery). When
    // active, the list sorts by distance and each card shows the km to the user.
    userLat: Double? = null,
    userLon: Double? = null,
    nearActive: Boolean = false,
    locating: Boolean = false,
    onToggleNearMe: () -> Unit = {},
) {
    // null = show all cities. rememberSaveable so the selection survives leaving/returning to the tab
    // (plain remember is discarded when the destination leaves composition on a tab switch).
    var selectedCity by rememberSaveable { mutableStateOf<String?>(null) }
    // Type filter — stored as the enum NAME (a String is Saveable; the enum isn't) so it also survives.
    var selectedTypeName by rememberSaveable { mutableStateOf<String?>(null) }
    val activeType = selectedTypeName?.let { name -> ShelterType.entries.firstOrNull { it.name == name } }
    var pendingDelete by remember { mutableStateOf<Shelter?>(null) }

    when {
        state.isLoading -> Centered(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text(stringResource(Res.string.shelters_loading), Modifier.padding(top = 8.dp))
            }
        }

        state.error -> Centered(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.shelters_error))
                Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(Res.string.retry))
                }
            }
        }

        state.shelters.isEmpty() -> Centered(modifier) {
            Text(stringResource(Res.string.shelters_empty))
        }

        else -> {
            // Cities parsed from each address tail (after the last comma); used for the filter chips.
            val cities = remember(state.shelters) {
                state.shelters.map { cityOf(it.address) }.filter { it.isNotBlank() }.distinct().sorted()
            }
            // The types actually present, so we don't show empty type chips.
            val types = remember(state.shelters) {
                ShelterType.entries.filter { t -> state.shelters.any { it.type == t } }
            }
            // If the current selection is no longer present (data changed), fall back to "all".
            val activeCity = selectedCity?.takeIf { it in cities }
            val hasCoords = userLat != null && userLon != null
            val filtered = state.shelters
                .filter { activeCity == null || cityOf(it.address) == activeCity }
                .filter { activeType == null || it.type == activeType }
                .let { list ->
                    // Near me: sort ascending by distance (revertible — off restores the source order).
                    if (nearActive && hasCoords) {
                        list.sortedBy { Geo.distanceKm(userLat, userLon, it.latitude, it.longitude) }
                    } else {
                        list
                    }
                }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.shelters_official_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Revertible "near me" toggle — mirrors the map FAB and the web pill; the label states
                // the current mode in text (never colour alone).
                item {
                    FilterChip(
                        selected = nearActive,
                        onClick = onToggleNearMe,
                        label = {
                            Text(
                                stringResource(
                                    when {
                                        locating -> Res.string.map_locating
                                        nearActive -> Res.string.map_show_all
                                        else -> Res.string.map_near_me
                                    },
                                ),
                            )
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
                if (types.size > 1) {
                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = activeType == null,
                                onClick = { selectedTypeName = null },
                                label = { Text(stringResource(Res.string.shelters_filter_all)) },
                            )
                            types.forEach { type ->
                                FilterChip(
                                    selected = activeType == type,
                                    onClick = { selectedTypeName = if (activeType == type) null else type.name },
                                    label = { Text(typeLabel(type)) },
                                )
                            }
                        }
                    }
                }
                if (cities.size > 1) {
                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = activeCity == null,
                                onClick = { selectedCity = null },
                                label = { Text(stringResource(Res.string.shelters_filter_all)) },
                            )
                            cities.forEach { city ->
                                FilterChip(
                                    selected = activeCity == city,
                                    onClick = { selectedCity = city },
                                    label = { Text(city) },
                                )
                            }
                        }
                    }
                }
                items(filtered, key = { it.id }) { shelter ->
                    // Show the distance only while near-me is active and we have a fix.
                    val dist = if (nearActive && hasCoords) {
                        Geo.distanceKm(userLat, userLon, shelter.latitude, shelter.longitude)
                    } else {
                        null
                    }
                    if (isModerator) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ShelterCard(shelter, distanceKm = dist)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onEditShelter(shelter) },
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                ) { Text(stringResource(Res.string.shelter_edit)) }
                                OutlinedButton(
                                    onClick = { pendingDelete = shelter },
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error,
                                    ),
                                ) { Text(stringResource(Res.string.shelter_delete)) }
                            }
                        }
                    } else {
                        ShelterCard(shelter, distanceKm = dist)
                    }
                }
            }

            pendingDelete?.let { target ->
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { pendingDelete = null },
                    title = { Text(stringResource(Res.string.shelter_delete_confirm_title)) },
                    text = { Text(stringResource(Res.string.shelter_delete_confirm_msg, target.name)) },
                    confirmButton = {
                        Button(onClick = {
                            onDeleteShelter(target)
                            pendingDelete = null
                        }) { Text(stringResource(Res.string.shelter_delete)) }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { pendingDelete = null }) {
                            Text(stringResource(Res.string.admin_cancel))
                        }
                    },
                )
            }
        }
    }
}

/** Reused by the map's marker bottom sheet, so it's public. */
@Composable
fun ShelterCard(shelter: Shelter, modifier: Modifier = Modifier, distanceKm: Double? = null) {
    val caller = rememberPhoneCaller()
    val mapLauncher = rememberMapLauncher()
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = shelter.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(typeLabel(shelter.type)) },
                )
                if (shelter.verified) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(Res.string.shelters_verified)) },
                        leadingIcon = {
                            Icon(
                                painterResource(Res.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
            Text(shelter.address, style = MaterialTheme.typography.bodyMedium)

            distanceKm?.let { km ->
                val shown = ((km * 10).roundToInt() / 10.0).toString()
                Text(
                    text = stringResource(Res.string.shelter_distance, shown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }

            if (shelter.accepts.isNotBlank()) {
                LabeledText(stringResource(Res.string.shelters_accepts), shelter.accepts)
            }
            shelter.hours?.let { LabeledText(stringResource(Res.string.shelters_hours), it) }
            shelter.lastVerified?.let {
                Text(
                    text = stringResource(Res.string.shelters_last_verified, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                shelter.contactPhone?.let { phone ->
                    Button(
                        onClick = { caller.call(phone) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text("${stringResource(Res.string.help_call)}  $phone")
                    }
                }
                // Opens the coordinates in the device's NATIVE maps app (Apple Maps on iOS) — never a
                // third-party maps app (App Store Guideline 4).
                OutlinedButton(
                    onClick = { mapLauncher.open(shelter.latitude, shelter.longitude, shelter.name) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(Res.string.shelter_directions))
                }
            }
        }
    }
}

@Composable
private fun LabeledText(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

/** City/town parsed from a shelter address tail (after the last comma); "" if none. */
private fun cityOf(address: String): String = address.substringAfterLast(',', "").trim()

@Composable
private fun typeLabel(type: ShelterType): String = stringResource(
    when (type) {
        ShelterType.ACOPIO -> Res.string.shelter_type_acopio
        ShelterType.ALBERGUE -> Res.string.shelter_type_albergue
        ShelterType.SALUD -> Res.string.shelter_type_salud
        ShelterType.AGUA -> Res.string.shelter_type_agua
        ShelterType.OTRO -> Res.string.shelter_type_otro
    },
)

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
