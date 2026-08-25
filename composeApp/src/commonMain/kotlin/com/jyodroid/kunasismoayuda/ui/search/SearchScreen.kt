package com.jyodroid.kunasismoayuda.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.jyodroid.kunasismoayuda.ui.platform.rememberPhoneCaller
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.jyodroid.kunasismoayuda.core.data.remote.defaultServerBaseUrl
import com.jyodroid.kunasismoayuda.core.domain.model.SafeCheckIn
import com.jyodroid.kunasismoayuda.core.domain.model.SearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchStatus
import com.jyodroid.kunasismoayuda.core.domain.model.SearchSubject
import com.jyodroid.kunasismoayuda.core.domain.util.TimeAgo
import com.jyodroid.kunasismoayuda.core.domain.util.TimeAgoUnit
import com.jyodroid.kunasismoayuda.core.domain.util.relativeAgo
import kotlin.time.Clock
import androidx.compose.material3.Icon
import org.jetbrains.compose.resources.painterResource
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.ic_add
import com.jyodroid.kunasismoayuda.resources.board_filter_all
import com.jyodroid.kunasismoayuda.resources.action_cancel
import com.jyodroid.kunasismoayuda.resources.help_call
import com.jyodroid.kunasismoayuda.resources.mod_delete
import com.jyodroid.kunasismoayuda.resources.mod_delete_confirm
import com.jyodroid.kunasismoayuda.resources.retry
import com.jyodroid.kunasismoayuda.resources.safe_empty
import com.jyodroid.kunasismoayuda.resources.safe_status
import com.jyodroid.kunasismoayuda.resources.safe_unverified
import com.jyodroid.kunasismoayuda.resources.search_tab_reports
import com.jyodroid.kunasismoayuda.resources.search_tab_safe
import com.jyodroid.kunasismoayuda.resources.time_days_ago
import com.jyodroid.kunasismoayuda.resources.time_hours_ago
import com.jyodroid.kunasismoayuda.resources.time_just_now
import com.jyodroid.kunasismoayuda.resources.time_minutes_ago
import com.jyodroid.kunasismoayuda.resources.search_empty
import com.jyodroid.kunasismoayuda.resources.search_error
import com.jyodroid.kunasismoayuda.resources.search_last_seen
import com.jyodroid.kunasismoayuda.resources.search_loading
import com.jyodroid.kunasismoayuda.resources.search_new
import com.jyodroid.kunasismoayuda.resources.search_photo_close
import com.jyodroid.kunasismoayuda.resources.search_photo_unavailable
import com.jyodroid.kunasismoayuda.resources.search_status_found
import com.jyodroid.kunasismoayuda.resources.search_status_lost
import com.jyodroid.kunasismoayuda.resources.search_subject_person
import com.jyodroid.kunasismoayuda.resources.search_subject_pet
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The two lists that live under "Búsqueda y reencuentro": lost/found reports, and safe check-ins. */
enum class ReunifyMode { REPORTS, SAFE }

@Composable
fun SearchScreen(
    state: SearchUiState,
    onSubjectChange: (SearchSubject?) -> Unit,
    onStatusChange: (SearchStatus?) -> Unit,
    onRetry: () -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
    // Non-null only for a logged-in moderator — shows a delete affordance on each report. Null hides it.
    onDelete: ((Int) -> Unit)? = null,
    // Public "I'm safe" reassurance list, shown under the "A salvo" toggle.
    safeState: SafeUiState = SafeUiState(),
    onSafeRetry: () -> Unit = {},
    // Non-null only for a logged-in moderator — a per-card delete for fake/abusive safe posts.
    onSafeDelete: ((Int) -> Unit)? = null,
) {
    // Photo currently shown full-screen (id + a description for accessibility); null = none.
    var fullscreen by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var mode by remember { mutableStateOf(ReunifyMode.REPORTS) }

    Box(modifier.fillMaxSize()) {
        // Cap the content width and centre it so cards don't stretch across wide screens (tablets,
        // foldables, desktop). On phones (< 640dp) this is a no-op — the column fills the width.
        Column(
            Modifier.fillMaxHeight().widthIn(max = 640.dp).align(Alignment.TopCenter),
        ) {
            // Reportes ↔ A salvo toggle (both live under Búsqueda y reencuentro).
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = mode == ReunifyMode.REPORTS,
                    onClick = { mode = ReunifyMode.REPORTS },
                    label = { Text(stringResource(Res.string.search_tab_reports)) },
                )
                FilterChip(
                    selected = mode == ReunifyMode.SAFE,
                    onClick = { mode = ReunifyMode.SAFE },
                    label = { Text(stringResource(Res.string.search_tab_safe)) },
                )
            }

            if (mode == ReunifyMode.SAFE) {
                SafeContent(safeState, onSafeRetry, onSafeDelete)
                return@Column
            }

            // Subject filter (pets / people).
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.subjectFilter == null,
                    onClick = { onSubjectChange(null) },
                    label = { Text(stringResource(Res.string.board_filter_all)) },
                )
                SearchSubject.entries.forEach { subject ->
                    FilterChip(
                        selected = state.subjectFilter == subject,
                        onClick = { onSubjectChange(if (state.subjectFilter == subject) null else subject) },
                        label = { Text("${subjectEmoji(subject)} ${stringResource(subjectLabel(subject))}") },
                    )
                }
            }
            // Status filter (lost / found).
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.statusFilter == null,
                    onClick = { onStatusChange(null) },
                    label = { Text(stringResource(Res.string.board_filter_all)) },
                )
                SearchStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { onStatusChange(if (state.statusFilter == status) null else status) },
                        label = { Text(stringResource(statusLabel(status))) },
                    )
                }
            }

            when {
                state.isLoading -> Centered {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(stringResource(Res.string.search_loading), Modifier.padding(top = 8.dp))
                    }
                }

                state.error -> Centered {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(Res.string.search_error))
                        Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }

                state.reports.isEmpty() -> Centered { Text(stringResource(Res.string.search_empty)) }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.reports, key = { it.id }) { report ->
                        SearchCard(
                            report = report,
                            onPhotoClick = { pid -> fullscreen = pid to report.title },
                            onDelete = onDelete?.let { del -> { del(report.id) } },
                        )
                    }
                }
            }
        }

        // "New report" only applies to lost/found; the safe list is check-in-from-SOS only.
        if (mode == ReunifyMode.REPORTS) {
            ExtendedFloatingActionButton(
                onClick = onNew,
                text = { Text(stringResource(Res.string.search_new)) },
                icon = { Icon(painterResource(Res.drawable.ic_add), contentDescription = null) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    fullscreen?.let { (photoId, title) ->
        FullScreenPhoto(
            photoId = photoId,
            description = title,
            onDismiss = { fullscreen = null },
        )
    }
}

/** Tapping a report photo opens it here, filling the screen; tap anywhere (or the ✕) to dismiss. */
@Composable
private fun FullScreenPhoto(photoId: Int, description: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClickLabel = null) { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            SubcomposeAsyncImage(
                model = "${defaultServerBaseUrl()}/api/photos/$photoId",
                contentDescription = description,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                },
                error = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("🖼️", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text = stringResource(Res.string.search_photo_unavailable),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
            )
            Text(
                text = "✕",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .heightIn(min = 48.dp)
                    .clickable(onClickLabel = stringResource(Res.string.search_photo_close)) { onDismiss() }
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun SearchCard(report: SearchReport, onPhotoClick: (Int) -> Unit, onDelete: (() -> Unit)? = null) {
    val caller = rememberPhoneCaller()
    var confirming by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportThumbnail(report = report, onPhotoClick = onPhotoClick)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${subjectEmoji(report.subject)}  ${report.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(statusLabel(report.status))) },
                )
                Text(
                    text = stringResource(Res.string.search_last_seen, report.lastSeen),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (report.description.isNotBlank()) {
                    Text(report.description, style = MaterialTheme.typography.bodyMedium)
                }
                report.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { caller.call(report.contactPhone) },
                    modifier = Modifier.padding(top = 4.dp).heightIn(min = 48.dp),
                ) {
                    Text("${stringResource(Res.string.help_call)}  ${report.contactPhone}")
                }
                // Moderator-only delete (anti-abuse) — shown only when a delete handler is provided.
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = { confirming = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(Res.string.mod_delete))
                    }
                }
            }
        }
    }

    if (confirming && onDelete != null) {
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

/** 88dp square that always renders: the photo (with loading/error placeholders) or, when there's no
 *  photo, a subject-emoji placeholder — so every card keeps a consistent layout. */
@Composable
private fun ReportThumbnail(report: SearchReport, onPhotoClick: (Int) -> Unit) {
    val base = Modifier.size(88.dp).clip(RoundedCornerShape(8.dp))
    val pid = report.photoId
    if (pid == null) {
        PlaceholderBox(base) { Text(subjectEmoji(report.subject), style = MaterialTheme.typography.headlineMedium) }
    } else {
        SubcomposeAsyncImage(
            model = "${defaultServerBaseUrl()}/api/photos/$pid",
            contentDescription = report.title,
            contentScale = ContentScale.Crop,
            modifier = base.clickable { onPhotoClick(pid) },
            loading = { PlaceholderBox(Modifier.fillMaxSize()) { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) } },
            error = { PlaceholderBox(Modifier.fillMaxSize()) { Text(subjectEmoji(report.subject), style = MaterialTheme.typography.headlineMedium) } },
        )
    }
}

@Composable
private fun PlaceholderBox(modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) { content() }
}

private fun subjectEmoji(subject: SearchSubject): String = when (subject) {
    SearchSubject.PET -> "🐾"
    SearchSubject.PERSON -> "🧑"
}

private fun subjectLabel(subject: SearchSubject): StringResource = when (subject) {
    SearchSubject.PET -> Res.string.search_subject_pet
    SearchSubject.PERSON -> Res.string.search_subject_person
}

private fun statusLabel(status: SearchStatus): StringResource = when (status) {
    SearchStatus.LOST -> Res.string.search_status_lost
    SearchStatus.FOUND -> Res.string.search_status_found
}

/** The public "A salvo" list: named community check-ins (name · city · relative time). Read-only for
 *  everyone; a logged-in moderator additionally gets a per-card delete for fake/abusive posts. */
@Composable
private fun SafeContent(state: SafeUiState, onRetry: () -> Unit, onDelete: ((Int) -> Unit)? = null) {
    when {
        state.isLoading -> Centered {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text(stringResource(Res.string.search_loading), Modifier.padding(top = 8.dp))
            }
        }

        state.error -> Centered {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.search_error))
                Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.retry))
                }
            }
        }

        state.checkIns.isEmpty() -> Centered { Text(stringResource(Res.string.safe_empty)) }

        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.safe_unverified),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            items(state.checkIns, key = { it.id }) { item ->
                SafeCard(item, onDelete = onDelete?.let { del -> { del(item.id) } })
            }
        }
    }
}

@Composable
private fun SafeCard(item: SafeCheckIn, onDelete: (() -> Unit)? = null) {
    val now = remember(item.id) { Clock.System.now().toEpochMilliseconds() }
    val place = item.region?.takeIf { it.isNotBlank() }
    var confirming by remember { mutableStateOf(false) }
    // "A salvo" is stated in text (never conveyed by the ✅ alone) for accessibility.
    val meta = listOfNotNull(
        stringResource(Res.string.safe_status),
        place,
        relativeAgoText(relativeAgo(now, item.createdAtEpochMs)),
    ).joinToString(" · ")
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "✅  ${item.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // Moderator-only delete (anti-abuse: fake / mocking posts) — shown only with a delete handler.
            if (onDelete != null) {
                OutlinedButton(
                    onClick = { confirming = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.padding(top = 4.dp).heightIn(min = 48.dp),
                ) {
                    Text(stringResource(Res.string.mod_delete))
                }
            }
        }
    }

    if (confirming && onDelete != null) {
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
private fun relativeAgoText(ago: TimeAgo): String = when (ago.unit) {
    TimeAgoUnit.JUST_NOW -> stringResource(Res.string.time_just_now)
    TimeAgoUnit.MINUTES -> stringResource(Res.string.time_minutes_ago, ago.count)
    TimeAgoUnit.HOURS -> stringResource(Res.string.time_hours_ago, ago.count)
    TimeAgoUnit.DAYS -> stringResource(Res.string.time_days_ago, ago.count)
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}
