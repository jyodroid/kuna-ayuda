package com.jyodroid.kunasismoayuda.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jyodroid.kunasismoayuda.core.domain.model.AdminAccount
import com.jyodroid.kunasismoayuda.resources.Res
import com.jyodroid.kunasismoayuda.resources.admin_add_submit
import com.jyodroid.kunasismoayuda.resources.admin_add_title
import com.jyodroid.kunasismoayuda.resources.admin_adding
import com.jyodroid.kunasismoayuda.resources.admin_cancel
import com.jyodroid.kunasismoayuda.resources.admin_delete
import com.jyodroid.kunasismoayuda.resources.admin_delete_confirm
import com.jyodroid.kunasismoayuda.resources.admin_delete_confirm_body
import com.jyodroid.kunasismoayuda.resources.admin_delete_confirm_title
import com.jyodroid.kunasismoayuda.resources.admin_email
import com.jyodroid.kunasismoayuda.resources.admin_empty
import com.jyodroid.kunasismoayuda.resources.admin_err_duplicate
import com.jyodroid.kunasismoayuda.resources.admin_err_email
import com.jyodroid.kunasismoayuda.resources.admin_err_generic
import com.jyodroid.kunasismoayuda.resources.admin_err_password
import com.jyodroid.kunasismoayuda.resources.admin_error
import com.jyodroid.kunasismoayuda.resources.admin_loading
import com.jyodroid.kunasismoayuda.resources.admin_password
import com.jyodroid.kunasismoayuda.resources.admin_role_admin
import com.jyodroid.kunasismoayuda.resources.admin_role_superadmin
import com.jyodroid.kunasismoayuda.resources.retry
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdminManagementScreen(
    state: AdminState,
    onLoad: () -> Unit,
    onCreate: (email: String, password: String) -> Unit,
    onDelete: (Int) -> Unit,
    onClearCreateError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { onLoad() }

    var pendingDelete by remember { mutableStateOf<AdminAccount?>(null) }

    when {
        state.isLoading && state.admins.isEmpty() -> Centered(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text(stringResource(Res.string.admin_loading), Modifier.padding(top = 8.dp))
            }
        }

        state.error && state.admins.isEmpty() -> Centered(modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.admin_error))
                Button(onClick = onLoad, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.retry))
                }
            }
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AddAdminCard(
                    isCreating = state.isCreating,
                    createError = state.createError,
                    onCreate = onCreate,
                    onClearError = onClearCreateError,
                )
            }
            if (state.admins.isEmpty()) {
                item { Text(stringResource(Res.string.admin_empty), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(state.admins, key = { it.id }) { account ->
                    AdminCard(
                        account = account,
                        isDeleting = state.deletingId == account.id,
                        onDelete = { pendingDelete = account },
                    )
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(Res.string.admin_delete_confirm_title)) },
            text = { Text(stringResource(Res.string.admin_delete_confirm_body, target.email)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target.id)
                    pendingDelete = null
                }) {
                    Text(
                        text = stringResource(Res.string.admin_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(Res.string.admin_cancel))
                }
            },
        )
    }
}

@Composable
private fun AddAdminCard(
    isCreating: Boolean,
    createError: CreateError?,
    onCreate: (email: String, password: String) -> Unit,
    onClearError: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(Res.string.admin_add_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; if (createError != null) onClearError() },
                label = { Text(stringResource(Res.string.admin_email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; if (createError != null) onClearError() },
                label = { Text(stringResource(Res.string.admin_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
            createError?.let { err ->
                Text(
                    text = stringResource(createErrorRes(err)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Button(
                onClick = {
                    onCreate(email, password)
                    // Clear the fields on submit; a validation error re-surfaces via createError.
                    if (createError == null) { email = ""; password = "" }
                },
                enabled = !isCreating,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.heightIn(min = 20.dp))
                    Text(stringResource(Res.string.admin_adding), modifier = Modifier.padding(start = 8.dp))
                } else {
                    Text(stringResource(Res.string.admin_add_submit))
                }
            }
        }
    }
}

@Composable
private fun AdminCard(
    account: AdminAccount,
    isDeleting: Boolean,
    onDelete: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = account.email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            stringResource(
                                if (account.isSuperAdmin) Res.string.admin_role_superadmin
                                else Res.string.admin_role_admin,
                            ),
                        )
                    },
                )
            }
            // The superadmin owner can't be removed (server rejects it) — don't offer it.
            if (!account.isSuperAdmin) {
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isDeleting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(Res.string.admin_delete))
                }
            }
        }
    }
}

private fun createErrorRes(err: CreateError): StringResource = when (err) {
    CreateError.INVALID_EMAIL -> Res.string.admin_err_email
    CreateError.SHORT_PASSWORD -> Res.string.admin_err_password
    CreateError.DUPLICATE -> Res.string.admin_err_duplicate
    CreateError.GENERIC -> Res.string.admin_err_generic
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
