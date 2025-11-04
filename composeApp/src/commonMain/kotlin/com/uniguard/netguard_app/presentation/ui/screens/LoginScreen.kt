package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import com.uniguard.netguard_app.core.PermissionType
import com.uniguard.netguard_app.core.SettingsType
import com.uniguard.netguard_app.core.rememberAppSettings
import kotlinx.coroutines.launch
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = rememberKoinViewModel<AuthViewModel>(),
    onLoginSuccess: () -> Unit,
) {
    val loginState by viewModel.loginState.collectAsState()
    val appSettings = rememberAppSettings()
    val coroutineScope = rememberCoroutineScope()


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // ⚙️ State untuk dialog konfirmasi permission
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    // Localized error messages
    val emptyFieldsError = stringResource(Res.string.login_error_empty_fields)

    LaunchedEffect(Unit) {
        val checkBattery = appSettings.checkPermission(PermissionType.BatteryOptimization)
        val checkNotif = appSettings.checkPermission(PermissionType.Notification)

        if (!checkBattery) showBatteryDialog = true
        if (!checkNotif) showNotificationDialog = true
    }


    // Handle login result
    LaunchedEffect(loginState) {
        when (loginState) {
            is ApiResult.Success -> {
                appSettings.open(SettingsType.BatteryOptimization, false)
                onLoginSuccess()
            }
            is ApiResult.Error -> {
                showErrorToast((loginState as ApiResult.Error).message)
            }
            else -> {}
        }
    }

    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo/Title
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.login_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Login Form
                OutlinedTextFieldWithError(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = stringResource(Res.string.login_email),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    error = emailError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextFieldWithError(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = stringResource(Res.string.login_password),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    error = passwordError,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                PrimaryButton(
                    text = stringResource(Res.string.login_button),
                    onClick = {
                        // Validation
                        emailError = if (email.isBlank()) emptyFieldsError else null
                        passwordError = if (password.isBlank()) emptyFieldsError else null

                        if (emailError == null && passwordError == null) {
                            viewModel.login(email, password)
                        }
                    },
                    isLoading = loginState is ApiResult.Loading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ⚙️ Alert Dialog: Battery Optimization
    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog = false },
            title = { Text("Battery Optimization") },
            text = { Text("This app needs to be excluded from battery optimization to run background monitoring. Open system dialog?") },
            confirmButton = {
                TextButton(onClick = {
                    showBatteryDialog = false
                    coroutineScope.launch {
                        appSettings.requestPermission(PermissionType.BatteryOptimization)
                    }
                }) {
                    Text("OK")
                }

            },
            dismissButton = {
                TextButton(onClick = { showBatteryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ⚙️ Alert Dialog: Notification Permission
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Notification Permission") },
            text = { Text("Allow this app to send you notifications?") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationDialog = false
                    coroutineScope.launch {
                        appSettings.requestPermission(PermissionType.Notification)
                    }
                }) {
                    Text("Allow")
                }

            },
            dismissButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}