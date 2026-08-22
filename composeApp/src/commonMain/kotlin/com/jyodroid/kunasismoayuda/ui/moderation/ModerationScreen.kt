package com.jyodroid.kunasismoayuda.ui.moderation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.ResourcePost
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.admin_manage_entry
import com.jyodroid.kunasismoayuda.resources.shelter_add_entry
import com.jyodroid.kunasismoayuda.resources.sos_resp_entry
import com.jyodroid.kunasismoayuda.resources.action_cancel
import com.jyodroid.kunasismoayuda.resources.mod_approve
import com.jyodroid.kunasismoayuda.resources.mod_delete
import com.jyodroid.kunasismoayuda.resources.mod_delete_confirm
import com.jyodroid.kunasismoayuda.resources.mod_empty
import com.jyodroid.kunasismoayuda.resources.mod_error
import com.jyodroid.kunasismoayuda.resources.mod_loading
import com.jyodroid.kunasismoayuda.resources.mod_fact_check
import com.jyodroid.kunasismoayuda.resources.mod_original
import com.jyodroid.kunasismoayuda.resources.mod_published_empty
import com.jyodroid.kunasismoayuda.resources.mod_reject
import com.jyodroid.kunasismoayuda.resources.mod_tab_pending
import com.jyodroid.kunasismoayuda.resources.mod_tab_published
import com.jyodroid.kunasismoayuda.resources.retry
import com.jyodroid.kunasismoayuda.ui.board.RiskFlagsBlock
import com.jyodroid.kunasismoayuda.ui.board.resourceTypeEmoji
import com.jyodroid.kunasismoayuda.ui.board.resourceTypeLabelRes
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModerationScreen(
    state: ModerationState,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onLoad: () -> Unit,
    onSelectTab: (ModerationTab) -> Unit,
    modifier: Modifier = Modifier,
    // Non-null only for a SUPERADMIN session — opens the admin-account console. Null hides the entry.
    onManageAdmins: (() -> Unit)? = null,
    // Opens the SOS responder view (available to any logged-in moderator). Null hides the entry.
    onSosResponder: (() -> Unit)? = null,
    // Opens the "add shelter/collection point" form (any logged-in moderator). Null hides the entry.
    onAddShelter: (() -> Unit)? = null,
) {
    LaunchedEffect(Unit) { onLoad() }

    Column(modifier.fillMaxSize()) {
        onAddShelter?.let { addShelter ->
            OutlinedButton(
                onClick = addShelter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text(stringResource(Res.string.shelter_add_entry))
            }
        }
        onSosResponder?.let { responder ->
            OutlinedButton(
                onClick = responder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text(stringResource(Res.string.sos_resp_entry))
            }
        }
        onManageAdmins?.let { manage ->
            OutlinedButton(
                onClick = manage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .heightIn(min = 48.dp),
            ) {
                Text(stringResource(Res.string.admin_manage_entry))
            }
        }
        // Pendientes (moderation queue) ↔ Publicados (live posts, deletable on the spot).
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.tab == ModerationTab.PENDING,
                onClick = { onSelectTab(ModerationTab.PENDING) },
                label = { Text(stringResource(Res.string.mod_tab_pending)) },
            )
            FilterChip(
                selected = state.tab == ModerationTab.PUBLISHED,
                onClick = { onSelectTab(ModerationTab.PUBLISHED) },
                label = { Text(stringResource(Res.string.mod_tab_published)) },
            )
        }
        Box(Modifier.weight(1f)) {
            ModerationContent(
                state = state,
                onApprove = onApprove,
                onReject = onReject,
                onLoad = onLoad,
            )
        }
    }
}

@Composable
private fun ModerationContent(
    state: ModerationState,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pending = state.tab == ModerationTab.PENDING
    val items = if (pending) state.pending else state.active
    when {
        state.isLoading -> Centered(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text(stringResource(Res.string.mod_loading), Modifier.padding(top = 8.dp))
            }
        }

        state.error && items.isEmpty() -> Centered(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.mod_error))
                Button(onClick = onLoad, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.retry))
                }
            }
        }

        items.isEmpty() -> Centered(modifier) {
            Text(stringResource(if (pending) Res.string.mod_empty else Res.string.mod_published_empty))
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { post ->
                if (pending) {
                    PendingCard(
                        post = post,
                        isActioning = state.actioningId == post.id,
                        onApprove = { onApprove(post.id) },
                        onReject = { onReject(post.id) },
                    )
                } else {
                    ActiveCard(
                        post = post,
                        isActioning = state.actioningId == post.id,
                        onDelete = { onReject(post.id) },
                    )
                }
            }
        }
    }
}

/** A published post with a single, confirmed "Eliminar" action (delete a live post immediately). */
@Composable
private fun ActiveCard(
    post: ResourcePost,
    isActioning: Boolean,
    onDelete: () -> Unit,
) {
    var confirming by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${resourceTypeEmoji(post.resourceType)}  ${stringResource(resourceTypeLabelRes(post.resourceType))} · ${post.region}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = post.kind.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (post.description.isNotBlank()) {
                Text(post.description, style = MaterialTheme.typography.bodyMedium)
            }
            post.contactPhone?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(
                onClick = { confirming = true },
                enabled = !isActioning,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 48.dp),
            ) {
                Text(stringResource(Res.string.mod_delete))
            }
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(Res.string.mod_delete)) },
            text = { Text(stringResource(Res.string.mod_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirming = false; onDelete() }) {
                    Text(stringResource(Res.string.mod_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun PendingCard(
    post: ResourcePost,
    isActioning: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${resourceTypeEmoji(post.resourceType)}  ${stringResource(resourceTypeLabelRes(post.resourceType))} · ${post.region}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = post.kind.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (post.description.isNotBlank()) {
                Text(post.description, style = MaterialTheme.typography.bodyMedium)
            }
            post.contactName?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            post.contactPhone?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            post.contactEmail?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }

            // The original pasted text, so the moderator can judge the AI's classification.
            post.rawText?.takeIf { it.isNotBlank() }?.let { raw ->
                Text(
                    text = stringResource(Res.string.mod_original),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = raw,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Google Fact Check signal (if any matched). A caution container, but the meaning is in the
            // heading + text — never colour alone. The moderator still decides; it never auto-rejects.
            post.factCheck?.takeIf { it.isNotBlank() }?.let { note ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            text = stringResource(Res.string.mod_fact_check),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            // AI risk flags (scam / unverified / no-source) — a moderator signal, never an auto-reject.
            RiskFlagsBlock(post.riskFlags, modifier = Modifier.padding(top = 6.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !isActioning,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                ) {
                    Text(stringResource(Res.string.mod_approve))
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isActioning,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                ) {
                    Text(stringResource(Res.string.mod_reject))
                }
            }
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
