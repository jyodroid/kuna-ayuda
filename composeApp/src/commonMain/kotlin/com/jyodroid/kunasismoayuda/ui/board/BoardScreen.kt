package com.jyodroid.kunasismoayuda.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.PostKind
import com.jyodroid.kunasismoayuda.core.domain.model.ResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.ResourceType
import androidx.compose.material3.Icon
import org.jetbrains.compose.resources.painterResource
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.ic_add
import com.jyodroid.kunasismoayuda.resources.board_email
import com.jyodroid.kunasismoayuda.resources.board_kind_offer
import com.jyodroid.kunasismoayuda.resources.board_kind_request
import com.jyodroid.kunasismoayuda.resources.board_empty
import com.jyodroid.kunasismoayuda.resources.board_error
import com.jyodroid.kunasismoayuda.resources.board_filter_all
import com.jyodroid.kunasismoayuda.resources.board_loading
import com.jyodroid.kunasismoayuda.resources.board_new
import com.jyodroid.kunasismoayuda.resources.board_offers
import com.jyodroid.kunasismoayuda.resources.board_paste_cta
import com.jyodroid.kunasismoayuda.resources.board_requests
import com.jyodroid.kunasismoayuda.resources.board_resolve
import com.jyodroid.kunasismoayuda.resources.board_search_cta
import com.jyodroid.kunasismoayuda.resources.board_unverified
import com.jyodroid.kunasismoayuda.resources.help_call
import com.jyodroid.kunasismoayuda.resources.retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun BoardScreen(
    state: BoardUiState,
    onKindChange: (PostKind) -> Unit,
    onTypeChange: (ResourceType?) -> Unit,
    onRetry: () -> Unit,
    onNewPost: () -> Unit,
    onPastePost: () -> Unit,
    onSearch: () -> Unit,
    onResolve: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Requests ↔ Offers as secondary tabs (binary kind filter).
            SecondaryTabRow(selectedTabIndex = state.kindFilter.ordinal) {
                Tab(
                    selected = state.kindFilter == PostKind.REQUEST,
                    onClick = { onKindChange(PostKind.REQUEST) },
                    text = { Text(stringResource(Res.string.board_requests)) },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
                Tab(
                    selected = state.kindFilter == PostKind.OFFER,
                    onClick = { onKindChange(PostKind.OFFER) },
                    text = { Text(stringResource(Res.string.board_offers)) },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
            }

            // Resource-type filter (All + each type). Kept in the VM state, so it survives tab switches.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.typeFilter == null,
                    onClick = { onTypeChange(null) },
                    label = { Text(stringResource(Res.string.board_filter_all)) },
                )
                ResourceType.entries.forEach { type ->
                    FilterChip(
                        selected = state.typeFilter == type,
                        onClick = { onTypeChange(if (state.typeFilter == type) null else type) },
                        label = { Text("${resourceTypeEmoji(type)} ${stringResource(resourceTypeLabelRes(type))}") },
                    )
                }
            }

            // Secondary actions: paste-and-classify + entry to Lost & Found (keeps us at 5 tabs).
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onPastePost, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.board_paste_cta))
                }
                TextButton(onClick = onSearch, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("🔎  ${stringResource(Res.string.board_search_cta)}")
                }
            }

            when {
                state.isLoading -> Centered {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(stringResource(Res.string.board_loading), Modifier.padding(top = 8.dp))
                    }
                }

                state.error -> Centered {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(Res.string.board_error))
                        Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }

                state.posts.isEmpty() -> Centered { Text(stringResource(Res.string.board_empty)) }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            text = stringResource(Res.string.board_unverified),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(state.posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            isOwned = post.id in state.ownedIds,
                            isResolving = state.resolvingId == post.id,
                            onResolve = { onResolve(post.id) },
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNewPost,
            text = { Text(stringResource(Res.string.board_new)) },
            icon = { Icon(painterResource(Res.drawable.ic_add), contentDescription = null) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostCard(
    post: ResourcePost,
    isOwned: Boolean,
    isResolving: Boolean,
    onResolve: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val caller = rememberPhoneCaller()
    var showSheet by remember { mutableStateOf(false) }

    // Compact, tappable card — long text is truncated here and shown in full in the detail sheet.
    Card(onClick = { showSheet = true }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PostHeader(post)
            if (post.description.isNotBlank()) {
                Text(
                    post.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            post.contactName?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = post.createdAt.substringBefore('T'),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PostActions(post, caller, uriHandler, isOwned, isResolving, onResolve)
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PostHeader(post)
                Text(
                    text = stringResource(
                        if (post.kind == PostKind.OFFER) Res.string.board_kind_offer else Res.string.board_kind_request,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (post.description.isNotBlank()) {
                    // Full text, no truncation.
                    Text(post.description, style = MaterialTheme.typography.bodyLarge)
                }
                post.contactName?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = post.createdAt.substringBefore('T'),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PostActions(post, caller, uriHandler, isOwned, isResolving, onResolve)
            }
        }
    }
}

@Composable
private fun PostHeader(post: ResourcePost) {
    Text(
        text = "${resourceTypeEmoji(post.resourceType)}  ${stringResource(resourceTypeLabelRes(post.resourceType))} · ${post.region}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

/** Contact + resolve actions, reused on the compact card and in the full-text detail sheet. */
@Composable
private fun PostActions(
    post: ResourcePost,
    caller: com.jyodroid.kunasismoayuda.ui.platform.PhoneCaller,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    isOwned: Boolean,
    isResolving: Boolean,
    onResolve: () -> Unit,
) {
    // Contact is opt-in (#3): show the call / email actions only when the poster shared them.
    post.contactPhone?.takeIf { it.isNotBlank() }?.let { phone ->
        Button(
            onClick = { caller.call(phone) },
            modifier = Modifier.padding(top = 4.dp).heightIn(min = 48.dp),
        ) {
            Text("${stringResource(Res.string.help_call)}  $phone")
        }
    }
    post.contactEmail?.takeIf { it.isNotBlank() }?.let { mail ->
        OutlinedButton(
            onClick = { uriHandler.openUri("mailto:$mail") },
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text("${stringResource(Res.string.board_email)}  $mail")
        }
    }
    // Device-gated resolve (#4): only the device that created this post sees this.
    if (isOwned) {
        OutlinedButton(
            onClick = onResolve,
            enabled = !isResolving,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(Res.string.board_resolve))
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
