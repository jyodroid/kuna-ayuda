package com.jyodroid.kunasismoayuda.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.ClassifiedPreview
import com.jyodroid.kunasismoayuda.core.domain.model.PostKind
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.board_kind_offer
import com.jyodroid.kunasismoayuda.resources.board_kind_request
import com.jyodroid.kunasismoayuda.resources.board_paste_kind_label
import com.jyodroid.kunasismoayuda.resources.board_paste_ai_note
import com.jyodroid.kunasismoayuda.resources.board_paste_error
import com.jyodroid.kunasismoayuda.resources.board_paste_field
import com.jyodroid.kunasismoayuda.resources.board_paste_howto
import com.jyodroid.kunasismoayuda.resources.board_collection_points
import com.jyodroid.kunasismoayuda.resources.board_paste_intro
import com.jyodroid.kunasismoayuda.resources.board_paste_sending
import com.jyodroid.kunasismoayuda.resources.board_paste_sent
import com.jyodroid.kunasismoayuda.resources.board_paste_submit
import com.jyodroid.kunasismoayuda.resources.board_paste_unreadable
import com.jyodroid.kunasismoayuda.resources.board_preview_confirm
import com.jyodroid.kunasismoayuda.resources.board_preview_edit
import com.jyodroid.kunasismoayuda.resources.board_preview_note
import com.jyodroid.kunasismoayuda.resources.board_preview_sending
import com.jyodroid.kunasismoayuda.resources.board_preview_title
import com.jyodroid.kunasismoayuda.resources.mod_fact_check
import org.jetbrains.compose.resources.stringResource

/**
 * Paste a social-media post (Instagram/Facebook/WhatsApp/X) describing a need or offer. Two steps:
 *  1. **Analyze** — the server sends the text to Claude, which extracts a structured summary shown
 *     back to the poster (nothing is saved yet).
 *  2. **Confirm** — the poster reviews it and confirms; only then is it queued for a moderator to
 *     approve before it goes public (anti-fraud).
 */
@Composable
fun ClassifyPostScreen(
    state: ClassifyState,
    onSubmit: (String) -> Unit,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onKindChange: (PostKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Text is hoisted to the screen (not inside the input branch) so it survives the analyze → preview
    // → edit round-trip: tapping "Edit" just flips back to the input, with the pasted text intact.
    var text by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.pendingText) {
        val pending = state.pendingText
        if (text.isBlank() && !pending.isNullOrBlank()) text = pending
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            state.sent -> SentCard()
            state.preview != null -> PreviewSection(state, onConfirm, onEdit)
            else -> InputSection(
                text = text,
                onTextChange = { text = it },
                state = state,
                onKindChange = onKindChange,
                onSubmit = { onSubmit(text) },
            )
        }
    }
}

@Composable
private fun InputSection(
    text: String,
    onTextChange: (String) -> Unit,
    state: ClassifyState,
    onKindChange: (PostKind) -> Unit,
    onSubmit: () -> Unit,
) {
    val busy = state.isSubmitting

    Text(stringResource(Res.string.board_paste_intro), style = MaterialTheme.typography.bodyLarge)

    // Poster picks REQUEST/OFFER up front so the AI doesn't have to guess it.
    Text(
        stringResource(Res.string.board_paste_kind_label),
        style = MaterialTheme.typography.labelLarge,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = state.kind == PostKind.REQUEST,
            onClick = { onKindChange(PostKind.REQUEST) },
            enabled = !busy,
            label = { Text(stringResource(Res.string.board_kind_request)) },
            modifier = Modifier.heightIn(min = 48.dp),
        )
        FilterChip(
            selected = state.kind == PostKind.OFFER,
            onClick = { onKindChange(PostKind.OFFER) },
            enabled = !busy,
            label = { Text(stringResource(Res.string.board_kind_offer)) },
            modifier = Modifier.heightIn(min = 48.dp),
        )
    }

    // How to turn an Instagram/Facebook post into text you can paste (links/screenshots don't work).
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(Res.string.board_paste_howto),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }

    OutlinedTextField(
        value = text,
        onValueChange = { if (it.length <= 2000) onTextChange(it) },
        label = { Text(stringResource(Res.string.board_paste_field)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 5,
        enabled = !busy,
    )

    Button(
        onClick = onSubmit,
        enabled = !busy && text.isNotBlank(),
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        Text(stringResource(Res.string.board_paste_submit))
    }

    Text(
        text = stringResource(Res.string.board_paste_ai_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    val status = when {
        state.isSubmitting -> stringResource(Res.string.board_paste_sending)
        state.unreadable -> stringResource(Res.string.board_paste_unreadable)
        state.error -> stringResource(Res.string.board_paste_error)
        else -> null
    }
    if (status != null) {
        StatusCard(
            text = status,
            container = if (state.error || state.unreadable) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            showSpinner = state.isSubmitting,
        )
    }
}

/** Step 2: the poster reviews what Claude extracted and confirms before it goes to a moderator. */
@Composable
private fun PreviewSection(state: ClassifyState, onConfirm: () -> Unit, onEdit: () -> Unit) {
    val preview = state.preview ?: return
    val busy = state.isConfirming

    Text(
        text = stringResource(Res.string.board_preview_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val title = buildString {
                append(resourceTypeEmoji(preview.resourceType))
                append("  ")
                append(stringResource(resourceTypeLabelRes(preview.resourceType)))
                if (preview.region.isNotBlank()) append(" · ${preview.region}")
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            val kindLabel = if (preview.kind == PostKind.OFFER) {
                stringResource(Res.string.board_kind_offer)
            } else {
                stringResource(Res.string.board_kind_request)
            }
            Text(
                text = kindLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (preview.description.isNotBlank()) {
                Text(preview.description, style = MaterialTheme.typography.bodyMedium)
            }
            if (preview.collectionPoints.isNotEmpty()) {
                Text(
                    stringResource(Res.string.board_collection_points),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                preview.collectionPoints.take(8).forEach { p ->
                    val line = buildString {
                        append(p.name)
                        if (p.address.isNotBlank()) append(" — ${p.address}")
                        if (p.hours.isNotBlank()) append(" · ${p.hours}")
                    }
                    Text("•  $line", style = MaterialTheme.typography.bodyMedium)
                }
            }
            preview.contactName?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            preview.contactPhone?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }

            // Google Fact Check signal (if any matched) — a caution, not a verdict; the moderator decides.
            preview.factCheck?.takeIf { it.isNotBlank() }?.let { note ->
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
        }
    }

    Text(
        text = stringResource(Res.string.board_preview_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onConfirm,
            enabled = !busy,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
        ) {
            Text(stringResource(Res.string.board_preview_confirm))
        }
        OutlinedButton(
            onClick = onEdit,
            enabled = !busy,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
        ) {
            Text(stringResource(Res.string.board_preview_edit))
        }
    }

    val status = when {
        state.isConfirming -> stringResource(Res.string.board_preview_sending)
        state.error -> stringResource(Res.string.board_paste_error)
        else -> null
    }
    if (status != null) {
        StatusCard(
            text = status,
            container = if (state.error) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            showSpinner = state.isConfirming,
        )
    }
}

@Composable
private fun SentCard() {
    StatusCard(
        text = stringResource(Res.string.board_paste_sent),
        container = MaterialTheme.colorScheme.secondaryContainer,
        showSpinner = false,
    )
}

@Composable
private fun StatusCard(text: String, container: androidx.compose.ui.graphics.Color, showSpinner: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (showSpinner) CircularProgressIndicator()
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
