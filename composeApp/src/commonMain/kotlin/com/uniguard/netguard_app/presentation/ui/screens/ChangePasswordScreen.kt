package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.ChangePasswordRequest
import com.uniguard.netguard_app.presentation.ui.theme.NetGuardTheme
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: AuthViewModel = rememberKoinViewModel<AuthViewModel>(),
    onNavigateBack: () -> Unit
) {
    val changePasswordState by viewModel.changePasswordState.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Handle success
    LaunchedEffect(changePasswordState) {
        if (changePasswordState is ApiResult.Success) {
            showSuccessDialog = true
        }
    }

    NetGuardTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.change_password)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = stringResource(Res.string.change_password_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // Description
                Text(
                    text = stringResource(Res.string.change_password_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Form
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current Password
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text(stringResource(Res.string.current_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // New Password
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(stringResource(Res.string.new_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Confirm New Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(stringResource(Res.string.confirm_new_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                        )

                        // Password mismatch error
                        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text(
                                text = stringResource(Res.string.passwords_do_not_match),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Loading/Error states
                        when (changePasswordState) {
                            is ApiResult.Loading -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(Res.string.changing_password))
                                }
                            }
                            is ApiResult.Error -> {
                                Text(
                                    text = (changePasswordState as ApiResult.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            else -> {}
                        }

                        // Change Password Button
                        Button(
                            onClick = {
                                if (currentPassword.isNotBlank() &&
                                    newPassword.isNotBlank() &&
                                    confirmPassword.isNotBlank() &&
                                    newPassword == confirmPassword) {
                                    viewModel.changePassword(
                                        ChangePasswordRequest(
                                            currentPassword = currentPassword,
                                            newPassword = newPassword
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentPassword.isNotBlank() &&
                                    newPassword.isNotBlank() &&
                                    confirmPassword.isNotBlank() &&
                                    newPassword == confirmPassword &&
                                    changePasswordState !is ApiResult.Loading
                        ) {
                            Text(stringResource(Res.string.change_password))
                        }
                    }
                }
            }
        }

        // Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onNavigateBack()
                },
                title = {
                    Text(stringResource(Res.string.password_changed_successfully))
                },
                text = {
                    Text(stringResource(Res.string.password_changed_description))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text(stringResource(Res.string.ok))
                    }
                }
            )
        }
    }
}