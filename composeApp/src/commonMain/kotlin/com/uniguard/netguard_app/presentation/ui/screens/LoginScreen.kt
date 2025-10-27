package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.background
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
                    text = "NetGuard",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Server Monitoring System",
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
                    label = "Email",
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
                    label = "Password",
                    error = passwordError,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                PrimaryButton(
                    text = "Login",
                    onClick = {
                        // Validation
                        emailError = if (email.isBlank()) "Email is required" else null
                        passwordError = if (password.isBlank()) "Password is required" else null

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
                        text = "Don't have an account? Register here",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}