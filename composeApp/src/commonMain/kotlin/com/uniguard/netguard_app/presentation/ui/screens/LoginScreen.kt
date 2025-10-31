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
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = rememberKoinViewModel<AuthViewModel>(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Localized error messages
    val emptyFieldsError = stringResource(Res.string.login_error_empty_fields)

    // Handle login result
    LaunchedEffect(loginState) {
        when (loginState) {
            is ApiResult.Success -> {
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

                // Toast Host for error messages
    //                ToastHost()

                Spacer(modifier = Modifier.height(24.dp))

                // Register Link
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = stringResource(Res.string.login_no_account) + " " + stringResource(Res.string.login_sign_up),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}