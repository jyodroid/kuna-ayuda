package com.jyodroid.kunasismoayuda.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.NewResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.PostKind
import com.jyodroid.kunasismoayuda.core.domain.model.ResourceType
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.board_contact_disclaimer
import com.jyodroid.kunasismoayuda.resources.board_field_description
import com.jyodroid.kunasismoayuda.resources.board_field_email
import com.jyodroid.kunasismoayuda.resources.board_field_name
import com.jyodroid.kunasismoayuda.resources.board_field_phone
import com.jyodroid.kunasismoayuda.resources.board_field_region
import com.jyodroid.kunasismoayuda.resources.board_kind_offer
import com.jyodroid.kunasismoayuda.resources.board_kind_request
import com.jyodroid.kunasismoayuda.resources.board_submit
import com.jyodroid.kunasismoayuda.resources.board_submit_error
import com.jyodroid.kunasismoayuda.resources.board_submitting
import com.jyodroid.kunasismoayuda.resources.expiry_notice_30d
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    createState: CreateState,
    onSubmit: (NewResourcePost) -> Unit,
    modifier: Modifier = Modifier,
) {
    var kind by remember { mutableStateOf(PostKind.REQUEST) }
    var type by remember { mutableStateOf(ResourceType.WATER) }
    var region by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    // Contact is optional (opt-in). Region is the only required field.
    val canSubmit = region.isNotBlank() && !createState.isSubmitting

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Kind
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = kind == PostKind.REQUEST,
                onClick = { kind = PostKind.REQUEST },
                label = { Text(stringResource(Res.string.board_kind_request)) },
            )
            FilterChip(
                selected = kind == PostKind.OFFER,
                onClick = { kind = PostKind.OFFER },
                label = { Text(stringResource(Res.string.board_kind_offer)) },
            )
        }

        // Resource type
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResourceType.entries.forEach { rt ->
                FilterChip(
                    selected = type == rt,
                    onClick = { type = rt },
                    label = { Text("${resourceTypeEmoji(rt)} ${stringResource(resourceTypeLabelRes(rt))}") },
                )
            }
        }

        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text(stringResource(Res.string.board_field_region)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 500) description = it },
            label = { Text(stringResource(Res.string.board_field_description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(Res.string.board_field_phone)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.board_field_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(Res.string.board_field_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        // Contact is optional AND public — make that explicit before they submit.
        Text(
            text = stringResource(Res.string.board_contact_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Posts auto-archive after 30 days (server expiry sweep) — set expectations up front.
        Text(
            text = stringResource(Res.string.expiry_notice_30d),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (createState.error) {
            Text(
                text = stringResource(Res.string.board_submit_error),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
            )
        }

        Button(
            onClick = {
                onSubmit(
                    NewResourcePost(
                        kind = kind,
                        resourceType = type,
                        region = region.trim(),
                        description = description.trim(),
                        contactPhone = phone.trim().ifBlank { null },
                        contactEmail = email.trim().ifBlank { null },
                        contactName = name.trim().ifBlank { null },
                    ),
                )
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            if (createState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(Res.string.board_submitting))
            } else {
                Text(stringResource(Res.string.board_submit))
            }
        }
    }
}
