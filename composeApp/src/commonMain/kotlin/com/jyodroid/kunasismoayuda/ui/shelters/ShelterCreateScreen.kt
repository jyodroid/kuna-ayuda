package com.jyodroid.kunasismoayuda.ui.shelters

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.NewShelter
import com.jyodroid.kunasismoayuda.core.domain.model.Shelter
import com.jyodroid.kunasismoayuda.core.domain.model.ShelterType
import com.jyodroid.kunasismoayuda.feature.map.DisasterMap
import com.jyodroid.kunasismoayuda.feature.map.MapMarker
import com.jyodroid.kunasismoayuda.feature.map.MarkerKind
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.shelter_add_error
import com.jyodroid.kunasismoayuda.resources.shelter_field_accepts
import com.jyodroid.kunasismoayuda.resources.shelter_field_address
import com.jyodroid.kunasismoayuda.resources.shelter_field_hours
import com.jyodroid.kunasismoayuda.resources.shelter_field_name
import com.jyodroid.kunasismoayuda.resources.shelter_field_phone
import com.jyodroid.kunasismoayuda.resources.shelter_field_type
import com.jyodroid.kunasismoayuda.resources.shelter_location_missing
import com.jyodroid.kunasismoayuda.resources.shelter_location_selected
import com.jyodroid.kunasismoayuda.resources.shelter_pick_location
import com.jyodroid.kunasismoayuda.resources.shelter_save
import com.jyodroid.kunasismoayuda.resources.shelter_submit
import com.jyodroid.kunasismoayuda.resources.shelter_submitting
import com.jyodroid.kunasismoayuda.resources.shelter_type_acopio
import com.jyodroid.kunasismoayuda.resources.shelter_type_agua
import com.jyodroid.kunasismoayuda.resources.shelter_type_albergue
import com.jyodroid.kunasismoayuda.resources.shelter_type_otro
import com.jyodroid.kunasismoayuda.resources.shelter_type_salud
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShelterCreateScreen(
    country: Country,
    state: ShelterAdminState,
    onSubmit: (NewShelter) -> Unit,
    modifier: Modifier = Modifier,
    // Non-null → edit an existing point: the form is pre-filled and the button saves changes.
    existing: Shelter? = null,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: ShelterType.ACOPIO) }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var accepts by remember { mutableStateOf(existing?.accepts ?: "") }
    var hours by remember { mutableStateOf(existing?.hours ?: "") }
    var phone by remember { mutableStateOf(existing?.contactPhone ?: "") }
    var picked by remember { mutableStateOf(existing?.let { it.latitude to it.longitude }) }

    val canSubmit = name.isNotBlank() && address.isNotBlank() && picked != null && !state.isSubmitting

    Box(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxHeight()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.shelter_field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                stringResource(Res.string.shelter_field_type),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.semantics { heading() },
            )
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShelterType.entries.forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(stringResource(shelterTypeLabelRes(t))) },
                    )
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(Res.string.shelter_field_address)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = accepts,
                onValueChange = { accepts = it },
                label = { Text(stringResource(Res.string.shelter_field_accepts)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it },
                label = { Text(stringResource(Res.string.shelter_field_hours)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(Res.string.shelter_field_phone)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Map location picker: tap to drop the pin; the marker colour tracks the chosen type.
            Text(
                text = picked?.let { (lat, lon) ->
                    stringResource(Res.string.shelter_location_selected, formatCoord(lat), formatCoord(lon))
                } ?: stringResource(Res.string.shelter_pick_location),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (picked == null) FontWeight.Normal else FontWeight.SemiBold,
            )
            Surface(
                modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(12.dp)),
                tonalElevation = 1.dp,
            ) {
                DisasterMap(
                    markers = picked?.let { (lat, lon) ->
                        listOf(MapMarker(id = "picked", latitude = lat, longitude = lon, kind = type.toMarkerKind(), label = null))
                    } ?: emptyList(),
                    onMarkerTap = {},
                    focusLat = picked?.first ?: country.centerLat,
                    focusLon = picked?.second ?: country.centerLon,
                    focusZoom = country.defaultZoom,
                    onMapTap = { lat, lon -> picked = lat to lon },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (state.error) {
                Text(
                    stringResource(Res.string.shelter_add_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (picked == null) {
                Text(
                    stringResource(Res.string.shelter_location_missing),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = {
                    val (lat, lon) = picked ?: return@Button
                    onSubmit(
                        NewShelter(
                            name = name.trim(),
                            type = type,
                            address = address.trim(),
                            latitude = lat,
                            longitude = lon,
                            accepts = accepts.trim(),
                            hours = hours.trim().ifBlank { null },
                            contactPhone = phone.trim().ifBlank { null },
                            country = country.code,
                        ),
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(if (existing == null) Res.string.shelter_submit else Res.string.shelter_save))
                }
            }
            if (state.isSubmitting) {
                Text(
                    stringResource(Res.string.shelter_submitting),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

private fun shelterTypeLabelRes(type: ShelterType): StringResource = when (type) {
    ShelterType.ACOPIO -> Res.string.shelter_type_acopio
    ShelterType.ALBERGUE -> Res.string.shelter_type_albergue
    ShelterType.SALUD -> Res.string.shelter_type_salud
    ShelterType.AGUA -> Res.string.shelter_type_agua
    ShelterType.OTRO -> Res.string.shelter_type_otro
}

private fun ShelterType.toMarkerKind(): MarkerKind = when (this) {
    ShelterType.ACOPIO -> MarkerKind.ACOPIO
    ShelterType.ALBERGUE -> MarkerKind.ALBERGUE
    ShelterType.SALUD -> MarkerKind.SALUD
    ShelterType.AGUA -> MarkerKind.AGUA
    ShelterType.OTRO -> MarkerKind.OTRO
}

/** Trim a coordinate to ~5 decimals (~1 m) for display without dragging in a formatter dependency. */
private fun formatCoord(value: Double): String {
    val scaled = (value * 100000).toLong()
    val whole = scaled / 100000
    val frac = kotlin.math.abs(scaled % 100000).toString().padStart(5, '0')
    return "$whole.$frac"
}
