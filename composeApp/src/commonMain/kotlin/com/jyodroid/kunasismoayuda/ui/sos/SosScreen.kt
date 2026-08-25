package com.jyodroid.kunasismoayuda.ui.sos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jyodroid.kunasismoayuda.core.beacon.BeaconState
import com.jyodroid.kunasismoayuda.ui.platform.rememberPhoneCaller
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.action_cancel
import com.jyodroid.kunasismoayuda.resources.sos_field_name
import com.jyodroid.kunasismoayuda.resources.sos_safe_confirm_body
import com.jyodroid.kunasismoayuda.resources.sos_safe_confirm_title
import com.jyodroid.kunasismoayuda.resources.sos_safe_name_hint
import com.jyodroid.kunasismoayuda.resources.sos_safe_publish
import com.jyodroid.kunasismoayuda.resources.sos_beacon_active
import com.jyodroid.kunasismoayuda.resources.sos_beacon_button
import com.jyodroid.kunasismoayuda.resources.sos_beacon_cancel
import com.jyodroid.kunasismoayuda.resources.sos_beacon_desc
import com.jyodroid.kunasismoayuda.resources.sos_beacon_light
import com.jyodroid.kunasismoayuda.resources.sos_beacon_sound
import com.jyodroid.kunasismoayuda.resources.sos_beacon_start
import com.jyodroid.kunasismoayuda.resources.sos_beacon_stop
import com.jyodroid.kunasismoayuda.resources.sos_beacon_warning_body
import com.jyodroid.kunasismoayuda.resources.sos_beacon_warning_title
import com.jyodroid.kunasismoayuda.resources.sos_button
import com.jyodroid.kunasismoayuda.resources.sos_call
import com.jyodroid.kunasismoayuda.resources.sos_error
import com.jyodroid.kunasismoayuda.resources.sos_field_message
import com.jyodroid.kunasismoayuda.resources.sos_field_phone
import com.jyodroid.kunasismoayuda.resources.sos_field_region
import com.jyodroid.kunasismoayuda.resources.sos_intro
import com.jyodroid.kunasismoayuda.resources.sos_locating
import com.jyodroid.kunasismoayuda.resources.sos_location_off
import com.jyodroid.kunasismoayuda.resources.sos_location_on
import com.jyodroid.kunasismoayuda.resources.sos_pending
import com.jyodroid.kunasismoayuda.resources.sos_queued
import com.jyodroid.kunasismoayuda.resources.sos_queued_safe
import com.jyodroid.kunasismoayuda.resources.sos_safe_button
import com.jyodroid.kunasismoayuda.resources.sos_safe_sent
import com.jyodroid.kunasismoayuda.resources.sos_sending
import com.jyodroid.kunasismoayuda.resources.sos_sent
import org.jetbrains.compose.resources.stringResource

@Composable
fun SosScreen(
    state: SosUiState,
    emergencyNumber: String,
    onSos: (region: String, message: String, phone: String, name: String) -> Unit,
    onSafe: (name: String, region: String) -> Unit,
    beacon: BeaconState,
    canFlash: Boolean,
    canSound: Boolean,
    onStartBeacon: (light: Boolean, sound: Boolean) -> Unit,
    onStopBeacon: () -> Unit,
    onToggleLight: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val caller = rememberPhoneCaller()
    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showSafeConfirm by remember { mutableStateOf(false) }

    val busy = state.phase == SosPhase.LOCATING || state.phase == SosPhase.SENDING
    val hasName = name.trim().isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(Res.string.sos_intro), style = MaterialTheme.typography.bodyLarge)

        if (state.pending > 0) {
            PendingBanner(state.pending)
        }

        Button(
            onClick = { onSos(region, message, phone, name) },
            enabled = !busy,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
            modifier = Modifier.fillMaxWidth().height(72.dp),
        ) {
            Text(stringResource(Res.string.sos_button), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 120) name = it },
            label = { Text(stringResource(Res.string.sos_field_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text(stringResource(Res.string.sos_field_region)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = message,
            onValueChange = { if (it.length <= 500) message = it },
            label = { Text(stringResource(Res.string.sos_field_message)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(Res.string.sos_field_phone)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // "I'm safe" is a PUBLIC reassurance check-in: it needs a name and a confirm step before it
        // publishes. Disabled until a name is entered.
        OutlinedButton(
            onClick = { showSafeConfirm = true },
            enabled = !busy && hasName,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(Res.string.sos_safe_button))
        }
        if (!hasName) {
            Text(
                stringResource(Res.string.sos_safe_name_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (showSafeConfirm) {
            val preview = name.trim() + if (region.trim().isNotEmpty()) " · ${region.trim()}" else ""
            AlertDialog(
                onDismissRequest = { showSafeConfirm = false },
                title = { Text(stringResource(Res.string.sos_safe_confirm_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(Res.string.sos_safe_confirm_body))
                        Text(preview, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showSafeConfirm = false
                        onSafe(name, region)
                    }) { Text(stringResource(Res.string.sos_safe_publish)) }
                },
                dismissButton = {
                    TextButton(onClick = { showSafeConfirm = false }) {
                        Text(stringResource(Res.string.action_cancel))
                    }
                },
            )
        }

        if (canFlash || canSound) {
            BeaconSection(
                beacon = beacon,
                canFlash = canFlash,
                canSound = canSound,
                onStart = onStartBeacon,
                onStop = onStopBeacon,
                onToggleLight = onToggleLight,
                onToggleSound = onToggleSound,
            )
        }

        Button(
            onClick = { caller.call(emergencyNumber) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(Res.string.sos_call, emergencyNumber))
        }

        StatusArea(state, emergencyNumber)
    }
}

/**
 * Offline emergency **light + sound beacon**: flashes the torch and sounds an alarm in SOS Morse so
 * nearby rescuers can find you when there's no signal. Battery-guarded — a confirmation warns it drains
 * the battery, and it auto-stops after a short burst (the beacon controller enforces the timeout).
 */
@Composable
private fun BeaconSection(
    beacon: BeaconState,
    canFlash: Boolean,
    canSound: Boolean,
    onStart: (light: Boolean, sound: Boolean) -> Unit,
    onStop: () -> Unit,
    onToggleLight: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
) {
    var showWarning by remember { mutableStateOf(false) }
    // Intended channels before starting (default to whatever the device supports).
    var wantLight by remember { mutableStateOf(canFlash) }
    var wantSound by remember { mutableStateOf(canSound) }

    if (beacon.running) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.sos_beacon_active, beacon.secondsRemaining),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                // Live channel toggles so the user can drop sound (be discreet / save battery) or light.
                if (canFlash) {
                    ToggleRow(stringResource(Res.string.sos_beacon_light), beacon.light, onToggleLight)
                }
                if (canSound) {
                    ToggleRow(stringResource(Res.string.sos_beacon_sound), beacon.sound, onToggleSound)
                }
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text(stringResource(Res.string.sos_beacon_stop), fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        OutlinedButton(
            onClick = {
                wantLight = canFlash
                wantSound = canSound
                showWarning = true
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.sos_beacon_button), fontWeight = FontWeight.Bold)
                Text(
                    stringResource(Res.string.sos_beacon_desc),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text(stringResource(Res.string.sos_beacon_warning_title)) },
            text = { Text(stringResource(Res.string.sos_beacon_warning_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showWarning = false
                    onStart(wantLight, wantSound)
                }) { Text(stringResource(Res.string.sos_beacon_start)) }
            },
            dismissButton = {
                TextButton(onClick = { showWarning = false }) {
                    Text(stringResource(Res.string.sos_beacon_cancel))
                }
            },
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/**
 * Always-visible reassurance that queued reports are not lost — shown whenever the outbox still has
 * items waiting, independent of the current send phase.
 */
@Composable
private fun PendingBanner(pending: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(
            text = stringResource(Res.string.sos_pending, pending),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun StatusArea(state: SosUiState, emergencyNumber: String) {
    val text = when (state.phase) {
        SosPhase.LOCATING -> stringResource(Res.string.sos_locating)
        SosPhase.SENDING -> stringResource(Res.string.sos_sending)
        SosPhase.SENT_SOS -> stringResource(Res.string.sos_sent, emergencyNumber)
        SosPhase.QUEUED_SOS -> stringResource(Res.string.sos_queued, emergencyNumber)
        SosPhase.SENT_SAFE -> stringResource(Res.string.sos_safe_sent)
        SosPhase.QUEUED_SAFE -> stringResource(Res.string.sos_queued_safe)
        SosPhase.ERROR -> stringResource(Res.string.sos_error, emergencyNumber)
        SosPhase.IDLE -> null
    } ?: return

    val container = when (state.phase) {
        SosPhase.ERROR -> MaterialTheme.colorScheme.errorContainer
        SosPhase.SENT_SOS, SosPhase.SENT_SAFE -> MaterialTheme.colorScheme.secondaryContainer
        SosPhase.QUEUED_SOS, SosPhase.QUEUED_SAFE -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Assertive },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (state.phase == SosPhase.LOCATING || state.phase == SosPhase.SENDING) {
                CircularProgressIndicator()
            }
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (state.phase == SosPhase.SENT_SOS || state.phase == SosPhase.QUEUED_SOS) {
                Text(
                    text = if (state.preciseLocation) {
                        stringResource(Res.string.sos_location_on)
                    } else {
                        stringResource(Res.string.sos_location_off)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
