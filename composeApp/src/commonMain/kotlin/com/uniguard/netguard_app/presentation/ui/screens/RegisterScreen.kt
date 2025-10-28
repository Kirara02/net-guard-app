package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = rememberKoinViewModel<AuthViewModel>(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val registerState by viewModel.registerState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var divisionError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Localized error messages
    val emptyFieldsError = stringResource(Res.string.register_error_empty_fields)
    val passwordMismatchError = stringResource(Res.string.register_error_password_mismatch)

    // Handle register result
    LaunchedEffect(registerState) {
        when (registerState) {
            is ApiResult.Success -> {
                onRegisterSuccess()
            }
            is ApiResult.Error -> {
                showErrorToast((registerState as ApiResult.Error).message)
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
            verticalArrangement = Arrangement.Top
        ) {
            // Title
            Text(
                text = stringResource(Res.string.register_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.register_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Form
            OutlinedTextFieldWithError(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = stringResource(Res.string.register_name),
                error = nameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldWithError(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = stringResource(Res.string.register_email),
                error = emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldWithError(
                value = division,
                onValueChange = {
                    division = it
                    divisionError = null
                },
                label = stringResource(Res.string.register_division),
                error = divisionError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldWithError(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = null
                },
                label = stringResource(Res.string.register_phone),
                error = phoneError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldWithError(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = stringResource(Res.string.register_password),
                error = passwordError,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldWithError(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = stringResource(Res.string.register_confirm_password),
                error = confirmPasswordError,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            PrimaryButton(
                text = stringResource(Res.string.register_button),
                onClick = {
                    // Validation
                    nameError = if (name.isBlank()) emptyFieldsError else null
                    emailError = if (email.isBlank()) emptyFieldsError else null
                    divisionError = if (division.isBlank()) emptyFieldsError else null
                    phoneError = if (phone.isBlank()) emptyFieldsError else null
                    passwordError = if (password.isBlank()) emptyFieldsError else null
                    confirmPasswordError = when {
                        confirmPassword.isBlank() -> emptyFieldsError
                        confirmPassword != password -> passwordMismatchError
                        else -> null
                    }

                    if (nameError == null && emailError == null && divisionError == null &&
                        phoneError == null && passwordError == null && confirmPasswordError == null) {
                        viewModel.register(name, email, password, division, phone)
                    }
                },
                isLoading = registerState is ApiResult.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = stringResource(Res.string.register_have_account) + " " + stringResource(Res.string.register_sign_in),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            }
        }
    }
}