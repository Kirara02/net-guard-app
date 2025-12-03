package com.uniguard.netguard_app.presentation.ui.screens.shared.settings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.User
import netguardapp.composeapp.generated.resources.Res
import netguardapp.composeapp.generated.resources.cancel
import netguardapp.composeapp.generated.resources.edit_profile_division
import netguardapp.composeapp.generated.resources.edit_profile_name
import netguardapp.composeapp.generated.resources.edit_profile_phone
import netguardapp.composeapp.generated.resources.edit_profile_title
import netguardapp.composeapp.generated.resources.edit_profile_updating
import netguardapp.composeapp.generated.resources.update
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentUser: User?,
    updateProfileState: ApiResult<User>,
    onDismiss: () -> Unit,
    onUpdateProfile: (name: String, division: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var division by remember { mutableStateOf(currentUser?.division ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.edit_profile_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.edit_profile_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = division,
                    onValueChange = { division = it },
                    label = { Text(stringResource(Res.string.edit_profile_division)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(Res.string.edit_profile_phone)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )

                when (updateProfileState) {
                    is ApiResult.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.edit_profile_updating))
                        }
                    }
                    is ApiResult.Error -> {
                        Text(
                            text = updateProfileState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && division.isNotBlank() && phone.isNotBlank()) {
                        onUpdateProfile(name, division, phone)
                    }
                },
                enabled = name.isNotBlank() && division.isNotBlank() && phone.isNotBlank() &&
                        updateProfileState !is ApiResult.Loading
            ) {
                Text(stringResource(Res.string.update))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = updateProfileState !is ApiResult.Loading
            ) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}