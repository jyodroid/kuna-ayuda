package com.jyodroid.kunasismoayuda.ui.sos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.jyodroid.kunasismoayuda.ui.platform.rememberPhoneCaller
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.SosReport
import com.jyodroid.kunasismoayuda.core.domain.model.SosStats
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.admin_cancel
import com.jyodroid.kunasismoayuda.resources.retry
import com.jyodroid.kunasismoayuda.resources.sos_resp_action_attended
import com.jyodroid.kunasismoayuda.resources.sos_resp_action_delete
import com.jyodroid.kunasismoayuda.resources.sos_resp_action_notified
import com.jyodroid.kunasismoayuda.resources.sos_resp_action_restore
import com.jyodroid.kunasismoayuda.resources.sos_resp_badge_safe
import com.jyodroid.kunasismoayuda.resources.sos_resp_badge_sos
import com.jyodroid.kunasismoayuda.resources.sos_resp_call
import com.jyodroid.kunasismoayuda.resources.sos_resp_delete_confirm_msg
import com.jyodroid.kunasismoayuda.resources.sos_resp_delete_confirm_title
import com.jyodroid.kunasismoayuda.resources.sos_resp_directions
import com.jyodroid.kunasismoayuda.resources.sos_resp_empty
import com.jyodroid.kunasismoayuda.resources.sos_resp_error
import com.jyodroid.kunasismoayuda.resources.sos_resp_filter_all
import com.jyodroid.kunasismoayuda.resources.sos_resp_filter_safe
import com.jyodroid.kunasismoayuda.resources.sos_resp_filter_sos
import com.jyodroid.kunasismoayuda.resources.sos_resp_handled_by
import com.jyodroid.kunasismoayuda.resources.sos_resp_loading
import com.jyodroid.kunasismoayuda.resources.sos_resp_no_location
import com.jyodroid.kunasismoayuda.resources.sos_resp_received
import com.jyodroid.kunasismoayuda.resources.sos_resp_stat_attended
import com.jyodroid.kunasismoayuda.resources.sos_resp_stat_safe_pending
import com.jyodroid.kunasismoayuda.resources.sos_resp_stat_sos_pending
import com.jyodroid.kunasismoayuda.resources.sos_resp_view_active
import com.jyodroid.kunasismoayuda.resources.sos_resp_view_archived
import org.jetbrains.compose.resources.stringResource

@Composable
fun SosResponderScreen(
    state: SosResponderUiState,
    onFilterChange: (SosStatus?) -> Unit,
    onViewChange: (Boolean) -> Unit,
    onArchive: (Int) -> Unit,
    onReopen: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { onRetry() }

    // Which report (if any) is pending a delete confirmation.
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    Column(modifier.fillMaxSize()) {
        state.stats?.let { StatsRow(it) }
        ViewToggle(showArchived = state.showArchived, onViewChange = onViewChange)
        FilterRow(selected = state.filter, onFilterChange = onFilterChange)

        when {
            state.isLoading -> Centered {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(stringResource(Res.string.sos_resp_loading), Modifier.padding(top = 8.dp))
                }
            }

            state.error && state.reports.isEmpty() -> Centered {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(Res.string.sos_resp_error))
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }

            state.reports.isEmpty() -> Centered { Text(stringResource(Res.string.sos_resp_empty)) }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(state.reports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        isActioning = state.actioningId == report.id,
                        actionsEnabled = state.actioningId == null,
                        onArchive = { onArchive(report.id) },
                        onReopen = { onReopen(report.id) },
                        onDeleteRequest = { pendingDeleteId = report.id },
                        modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
                    )
                }
            }
        }
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(stringResource(Res.string.sos_resp_delete_confirm_title)) },
            text = { Text(stringResource(Res.string.sos_resp_delete_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(id)
                    pendingDeleteId = null
                }) {
                    Text(
                        stringResource(Res.string.sos_resp_action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(stringResource(Res.string.admin_cancel))
                }
            },
        )
    }
}

@Composable
private fun StatsRow(stats: SosStats) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // SOS still unattended is the urgent number — highlight it when > 0.
        StatCell(
            value = stats.pendingSos,
            label = stringResource(Res.string.sos_resp_stat_sos_pending),
            emphasize = stats.pendingSos > 0,
            modifier = Modifier.weight(1f),
        )
        StatCell(
            value = stats.pendingSafe,
            label = stringResource(Res.string.sos_resp_stat_safe_pending),
            modifier = Modifier.weight(1f),
        )
        StatCell(
            value = stats.handledTotal,
            label = stringResource(Res.string.sos_resp_stat_attended),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCell(value: Int, label: String, modifier: Modifier = Modifier, emphasize: Boolean = false) {
    val container = if (emphasize) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val onContainer = if (emphasize) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(modifier = modifier, color = container, contentColor = onContainer, shape = MaterialTheme.shapes.medium) {
        Column(
            Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ViewToggle(showArchived: Boolean, onViewChange: (Boolean) -> Unit) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        SegmentedButton(
            selected = !showArchived,
            onClick = { onViewChange(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) { Text(stringResource(Res.string.sos_resp_view_active)) }
        SegmentedButton(
            selected = showArchived,
            onClick = { onViewChange(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) { Text(stringResource(Res.string.sos_resp_view_archived)) }
    }
}

@Composable
private fun FilterRow(selected: SosStatus?, onFilterChange: (SosStatus?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == SosStatus.SOS,
            onClick = { onFilterChange(SosStatus.SOS) },
            label = { Text(stringResource(Res.string.sos_resp_filter_sos)) },
        )
        FilterChip(
            selected = selected == SosStatus.SAFE,
            onClick = { onFilterChange(SosStatus.SAFE) },
            label = { Text(stringResource(Res.string.sos_resp_filter_safe)) },
        )
        FilterChip(
            selected = selected == null,
            onClick = { onFilterChange(null) },
            label = { Text(stringResource(Res.string.sos_resp_filter_all)) },
        )
    }
}

@Composable
private fun ReportCard(
    report: SosReport,
    isActioning: Boolean,
    actionsEnabled: Boolean,
    onArchive: () -> Unit,
    onReopen: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val caller = rememberPhoneCaller()
    val isSos = report.status == SosStatus.SOS
    // Danger reports use the error container so they stand out; SAFE uses a calm surface. The badge
    // text also names the status, so meaning is never carried by color alone.
    val container = if (isSos) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val onContainer = if (isSos) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier,
        color = container,
        contentColor = onContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(if (isSos) Res.string.sos_resp_badge_sos else Res.string.sos_resp_badge_safe),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            report.region?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
            report.message?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = stringResource(Res.string.sos_resp_received, formatTime(report.createdAt)),
                style = MaterialTheme.typography.labelSmall,
            )
            // On archived cards, show who attended/notified it.
            if (report.isHandled) {
                report.handledBy?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = stringResource(Res.string.sos_resp_handled_by, it),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Contact + map row.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val lat = report.latitude
                val lon = report.longitude
                if (lat != null && lon != null) {
                    OutlinedButton(
                        onClick = {
                            uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(Res.string.sos_resp_directions))
                    }
                } else {
                    Text(
                        text = stringResource(Res.string.sos_resp_no_location),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                }
                report.contactPhone?.takeIf { it.isNotBlank() }?.let { phone ->
                    OutlinedButton(
                        onClick = { caller.call(phone) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(Res.string.sos_resp_call))
                    }
                }
            }

            // Lifecycle row: active -> archive; archived -> restore / delete.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!report.isHandled) {
                    Button(
                        onClick = onArchive,
                        enabled = actionsEnabled && !isActioning,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(if (isSos) Res.string.sos_resp_action_attended else Res.string.sos_resp_action_notified))
                    }
                } else {
                    Button(
                        onClick = onReopen,
                        enabled = actionsEnabled && !isActioning,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(Res.string.sos_resp_action_restore))
                    }
                    OutlinedButton(
                        onClick = onDeleteRequest,
                        enabled = actionsEnabled && !isActioning,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(Res.string.sos_resp_action_delete))
                    }
                }
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

/** Trim the server's ISO timestamp ("2026-08-15T14:30:00…") to "2026-08-15 14:30" for display. */
private fun formatTime(iso: String): String {
    val cleaned = iso.replace('T', ' ')
    val dot = cleaned.indexOf('.')
    val noFraction = if (dot >= 0) cleaned.substring(0, dot) else cleaned
    // Drop trailing seconds if present (…:mm:ss -> …:mm).
    return if (noFraction.count { it == ':' } >= 2) noFraction.substringBeforeLast(':') else noFraction
}
