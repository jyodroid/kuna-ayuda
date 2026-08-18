package com.jyodroid.kunasismoayuda.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jyodroid.kunasismoayuda.core.domain.model.NewSearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchStatus
import com.jyodroid.kunasismoayuda.core.domain.model.SearchSubject
import com.jyodroid.kunasismoayuda.media.PickedImage
import com.jyodroid.kunasismoayuda.media.rememberImagePicker
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.board_field_name
import com.jyodroid.kunasismoayuda.resources.board_field_phone
import com.jyodroid.kunasismoayuda.resources.board_submit
import com.jyodroid.kunasismoayuda.resources.board_submit_error
import com.jyodroid.kunasismoayuda.resources.board_submitting
import com.jyodroid.kunasismoayuda.resources.expiry_notice_30d
import com.jyodroid.kunasismoayuda.resources.search_disclaimer
import com.jyodroid.kunasismoayuda.resources.search_field_description
import com.jyodroid.kunasismoayuda.resources.search_field_last_seen
import com.jyodroid.kunasismoayuda.resources.search_field_notes
import com.jyodroid.kunasismoayuda.resources.search_field_title
import com.jyodroid.kunasismoayuda.resources.search_photo_camera
import com.jyodroid.kunasismoayuda.resources.search_photo_change
import com.jyodroid.kunasismoayuda.resources.search_photo_gallery
import com.jyodroid.kunasismoayuda.resources.search_status_found
import com.jyodroid.kunasismoayuda.resources.search_status_lost
import com.jyodroid.kunasismoayuda.resources.search_subject_person
import com.jyodroid.kunasismoayuda.resources.search_subject_pet
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchCreateScreen(
    createState: SearchCreateState,
    onSubmit: (report: NewSearchReport, photo: ByteArray?, mime: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var subject by remember { mutableStateOf(SearchSubject.PET) }
    var status by remember { mutableStateOf(SearchStatus.LOST) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var lastSeen by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf<PickedImage?>(null) }

    val picker = rememberImagePicker { picked = it }

    val canSubmit = title.isNotBlank() && lastSeen.isNotBlank() && phone.isNotBlank() && !createState.isSubmitting

    Box(modifier.fillMaxSize()) {
      // Cap width + centre so the form doesn't stretch across wide screens (no-op on phones).
      Column(
        modifier = Modifier.fillMaxHeight().widthIn(max = 640.dp).align(Alignment.TopCenter)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Subject: pet / person
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = subject == SearchSubject.PET,
                onClick = { subject = SearchSubject.PET },
                label = { Text("🐾 ${stringResource(Res.string.search_subject_pet)}") },
            )
            FilterChip(
                selected = subject == SearchSubject.PERSON,
                onClick = { subject = SearchSubject.PERSON },
                label = { Text("🧑 ${stringResource(Res.string.search_subject_person)}") },
            )
        }
        // Status: lost / found
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = status == SearchStatus.LOST,
                onClick = { status = SearchStatus.LOST },
                label = { Text(stringResource(Res.string.search_status_lost)) },
            )
            FilterChip(
                selected = status == SearchStatus.FOUND,
                onClick = { status = SearchStatus.FOUND },
                label = { Text(stringResource(Res.string.search_status_found)) },
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 160) title = it },
            label = { Text(stringResource(Res.string.search_field_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = lastSeen,
            onValueChange = { if (it.length <= 160) lastSeen = it },
            label = { Text(stringResource(Res.string.search_field_last_seen)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 800) description = it },
            label = { Text(stringResource(Res.string.search_field_description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(Res.string.search_field_notes)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(Res.string.board_field_phone)) },
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

        // Photo picker + preview
        picked?.let { img ->
            AsyncImage(
                model = img.bytes,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(8.dp)),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { picker.pickFromGallery() },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text("🖼️ ${stringResource(if (picked == null) Res.string.search_photo_gallery else Res.string.search_photo_change)}")
            }
            if (picker.cameraAvailable) {
                OutlinedButton(
                    onClick = { picker.captureFromCamera() },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text("📷 ${stringResource(Res.string.search_photo_camera)}")
                }
            }
        }

        Text(
            text = stringResource(Res.string.search_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Reports auto-archive after 30 days (server expiry sweep) — set expectations up front.
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
                    NewSearchReport(
                        subject = subject,
                        status = status,
                        title = title.trim(),
                        description = description.trim(),
                        lastSeen = lastSeen.trim(),
                        notes = notes.trim().ifBlank { null },
                        contactPhone = phone.trim(),
                        contactName = name.trim().ifBlank { null },
                    ),
                    picked?.bytes,
                    picked?.mime,
                )
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 48.dp),
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
}
