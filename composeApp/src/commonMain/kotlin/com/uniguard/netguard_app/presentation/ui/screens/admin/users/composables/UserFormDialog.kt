package com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Group
import com.uniguard.netguard_app.domain.model.User
import netguardapp.composeapp.generated.resources.Res
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormDialog(
    title: String,
    user: User? = null,
    currentUserRole: String,
    groups: List<Group>,
    preSelectedGroup: Group? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, password: String?, division: String, phone: String, role: String, groupId: String?) -> Unit,
    apiState: ApiResult<*>
) {

    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var division by remember { mutableStateOf(user?.division ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "USER") }

    val isSuperAdmin = currentUserRole.equals("SUPER_ADMIN", ignoreCase = true)

    var selectedGroupId by remember {
        mutableStateOf(preSelectedGroup?.id ?: user?.group?.id.orEmpty())
    }
    var selectedGroupName by remember {
        mutableStateOf(
            preSelectedGroup?.name
                ?: user?.group?.name
                ?: "No Group"
        )
    }
    var showGroupDropdown by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var showPasswordField by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val roles = remember(currentUserRole, selectedGroupId) {
        if (currentUserRole.lowercase() == "super_admin") {
            if (selectedGroupId.isNotBlank())
                listOf("User", "Admin")
            else
                listOf("User", "Admin", "Super Admin")
        } else {
            listOf("User", "Admin")
        }
    }

    val isLockedGroup = preSelectedGroup != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isSuperAdmin) {
                    ExposedDropdownMenuBox(
                        expanded = showGroupDropdown,
                        onExpandedChange = { showGroupDropdown = !showGroupDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedGroupName,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isLockedGroup,
                            label = { Text("Group") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGroupDropdown)
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = showGroupDropdown,
                            onDismissRequest = { showGroupDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.sa_dashboard_no_group)) },
                                onClick = {
                                    selectedGroupId = ""
                                    selectedGroupName = "No Group"
                                    showGroupDropdown = false
                                }
                            )

                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        selectedGroupId = group.id
                                        selectedGroupName = group.name

                                        if (normalizeRole(role) == "SUPER_ADMIN") {
                                            role = "User"
                                        }

                                        showGroupDropdown = false
                                    }
                                )

                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.users_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(Res.string.users_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                if (user == null) { // Only show password field for new users
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(Res.string.users_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(Res.string.users_confirm_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                } else if (isSuperAdmin || user.role.lowercase() == "user") { // Show password field for editing USER role
                    if (showPasswordField) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(Res.string.users_new_password)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(stringResource(Res.string.users_confirm_new_password)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            }
                        )
                    } else {
                        TextButton(
                            onClick = { showPasswordField = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.users_change_password))
                        }
                    }
                }

                OutlinedTextField(
                    value = division,
                    onValueChange = { division = it },
                    label = { Text(stringResource(Res.string.users_division)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(Res.string.users_phone)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { roleOption ->
                            DropdownMenuItem(
                                text = { Text(roleOption) },
                                onClick = {
                                    role = roleOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                when (apiState) {
                    is ApiResult.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.users_processing))
                        }
                    }
                    is ApiResult.Error -> {
                        Text(
                            text = apiState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {
                        // Show password mismatch error if passwords don't match
                        if ((user == null && password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword) ||
                            (user != null && user.role.lowercase() == "user" && showPasswordField &&
                                    password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword)) {
                            Text(
                                text = stringResource(Res.string.users_passwords_not_match),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isGroupRequired = isSuperAdmin && normalizeRole(role) != "SUPER_ADMIN"


            TextButton(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        val passwordToUse = if (user == null) {
                            // For new users, require password and confirmation
                            if (password.isBlank() || confirmPassword.isBlank()) return@TextButton
                            if (password != confirmPassword) return@TextButton
                            password
                        } else if (user.role.lowercase() == "user" && showPasswordField) {
                            // For editing USER role with password change
                            if (password.isNotBlank() && confirmPassword.isNotBlank()) {
                                if (password != confirmPassword) return@TextButton
                                password
                            } else {
                                null // No password change
                            }
                        } else {
                            null // No password for admin editing
                        }

                        val finalGroupId = when (normalizeRole(role)) {
                            "SUPER_ADMIN" -> null
                            else -> selectedGroupId
                        }

                        onConfirm(
                            name,
                            email,
                            password.ifBlank { null },
                            division,
                            phone,
                            normalizeRole(role),
                            finalGroupId
                        )
                    }
                },
                enabled =
                    name.isNotBlank() &&
                            email.isNotBlank() &&
                            (!isGroupRequired || selectedGroupId.isNotBlank()) &&
                            (user != null || (password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword)) &&
                            (user == null ||
                                    user.role.lowercase() != "user" ||
                                    !showPasswordField ||
                                    (password.isBlank() && confirmPassword.isBlank()) ||
                                    (password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword)) &&
                            apiState !is ApiResult.Loading
            ) {
                Text(if (user == null) stringResource(Res.string.users_create) else stringResource(Res.string.users_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = apiState !is ApiResult.Loading) {
                Text(stringResource(Res.string.users_cancel))
            }
        }
    )
}

private fun normalizeRole(role: String): String =
    role.uppercase().replace(" ", "_")