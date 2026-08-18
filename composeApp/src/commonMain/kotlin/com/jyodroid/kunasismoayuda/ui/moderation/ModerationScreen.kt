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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.jyodroid.kunasismoayuda.resources.mod_approve
import com.jyodroid.kunasismoayuda.resources.mod_empty
import com.jyodroid.kunasismoayuda.resources.mod_error
import com.jyodroid.kunasismoayuda.resources.mod_loading
import com.jyodroid.kunasismoayuda.resources.mod_fact_check
import com.jyodroid.kunasismoayuda.resources.mod_original
import com.jyodroid.kunasismoayuda.resources.mod_reject
import com.jyodroid.kunasismoayuda.resources.retry
import com.jyodroid.kunasismoayuda.ui.board.resourceTypeEmoji
import com.jyodroid.kunasismoayuda.ui.board.resourceTypeLabelRes
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModerationScreen(
    state: ModerationState,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onLoad: () -> Unit,
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
    when {
        state.isLoading -> Centered(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text(stringResource(Res.string.mod_loading), Modifier.padding(top = 8.dp))
            }
        }

        state.error && state.pending.isEmpty() -> Centered(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.mod_error))
                Button(onClick = onLoad, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.retry))
                }
            }
        }

        state.pending.isEmpty() -> Centered(modifier) { Text(stringResource(Res.string.mod_empty)) }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.pending, key = { it.id }) { post ->
                PendingCard(
                    post = post,
                    isActioning = state.actioningId == post.id,
                    onApprove = { onApprove(post.id) },
                    onReject = { onReject(post.id) },
                )
            }
        }
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
