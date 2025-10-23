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
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.ui.theme.NetGuardTheme
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AuthViewModel = rememberKoinViewModel<AuthViewModel>(),
    onNavigateBack: () -> Unit
) {
    val userProfileState by viewModel.userProfileState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Load user settings when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    NetGuardTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (userProfileState) {
                    is ApiResult.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    "Loading settings...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    is ApiResult.Success -> {
                        val user = (userProfileState as ApiResult.Success).data
                        ProfileCard(
                            name = user.name,
                            email = user.email,
                            division = user.division,
                            phone = user.phone,
                            role = user.role.uppercase()
                        )
                    }
                    is ApiResult.Error -> {
                        val error = (userProfileState as ApiResult.Error).message
                        // Fallback to cached user data if available
                        currentUser?.let { user ->
                            ProfileCard(
                                name = user.name,
                                email = user.email,
                                division = user.division,
                                phone = user.phone,
                                role = user.role.uppercase()
                            )
                        } ?: run {
                            // Show error if no cached data
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                ErrorMessage(
                                    message = error,
                                    onRetry = { viewModel.loadUserProfile() }
                                )
                            }
                        }
                    }
                    is ApiResult.Initial -> {
                        // Show cached data initially
                        currentUser?.let { user ->
                            ProfileCard(
                                name = user.name,
                                email = user.email,
                                division = user.division,
                                phone = user.phone,
                                role = user.role.uppercase()
                            )
                        } ?: run {
                            // Show loading if no cached data
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Settings Options
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsItem(
                        title = "Monitoring Settings",
                        subtitle = "Configure server monitoring interval",
                        onClick = { /* TODO */ }
                    )

                    SettingsItem(
                        title = "Notification Settings",
                        subtitle = "Configure push notifications",
                        onClick = { /* TODO */ }
                    )

                    SettingsItem(
                        title = "Change Password",
                        subtitle = "Update your account password",
                        onClick = { /* TODO */ }
                    )

                    SettingsItem(
                        title = "App Settings",
                        subtitle = "Theme, language, and preferences",
                        onClick = { /* TODO */ }
                    )

                    SettingsItem(
                        title = "About",
                        subtitle = "App version and information",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                SecondaryButton(
                    text = "Logout",
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}