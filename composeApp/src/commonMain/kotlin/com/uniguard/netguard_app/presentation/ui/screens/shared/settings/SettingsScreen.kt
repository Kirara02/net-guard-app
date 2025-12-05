package com.uniguard.netguard_app.presentation.ui.screens.shared.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.di.getKoinInstance
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.UserRole
import com.uniguard.netguard_app.domain.service.UserSessionService
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.ui.screens.shared.settings.composables.EditProfileDialog
import com.uniguard.netguard_app.presentation.ui.screens.shared.settings.composables.IntervalPickerDialog
import com.uniguard.netguard_app.presentation.ui.screens.shared.settings.composables.LanguagePickerDialog
import com.uniguard.netguard_app.presentation.ui.screens.shared.settings.composables.ProfileCard
import com.uniguard.netguard_app.presentation.ui.screens.shared.settings.composables.SettingItem
import com.uniguard.netguard_app.presentation.viewmodel.user.AuthViewModel
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AuthViewModel,
    onNavigateToAbout: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val userProfileState by viewModel.userProfileState.collectAsState()
    val updateProfileState by viewModel.updateProfileState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val appPreferences = getKoinInstance<AppPreferences>()
    val userSessionService =  getKoinInstance<UserSessionService>()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    val isDarkMode by appPreferences.themePreferenceFlow.collectAsState(initial = false)

    // Load user settings when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Handle update profile success
    LaunchedEffect(updateProfileState) {
        if (updateProfileState is ApiResult.Success) {
            showEditProfileDialog = false
            viewModel.loadUserProfile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
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
                                stringResource(Res.string.settings_loading),
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
                        onEditClick = { showEditProfileDialog = true }
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
                            onEditClick = { showEditProfileDialog = true }
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
                            onEditClick = { showEditProfileDialog = true }
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
                // Theme Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(Res.string.dark_mode),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(Res.string.dark_mode_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { newValue ->
                            appPreferences.saveThemePreference(newValue)
                            // Force app restart or theme update
                            // Note: In a real app, you might want to use a ViewModel or state management
                            // to trigger theme changes across the app
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(Res.string.language), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(Res.string.select_language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = { showLangDialog = true }) {
                        Text(stringResource(Res.string.change))
                    }
                }

                // Monitoring Interval Setting
                AnimatedVisibility(
                    visible = userSessionService.getUserRole() != UserRole.SUPER_ADMIN
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(Res.string.monitoring_interval),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    stringResource(Res.string.monitoring_interval_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            var showIntervalDialog by remember { mutableStateOf(false) }
                            val currentInterval =
                                remember { mutableStateOf(appPreferences.getMonitoringInterval()) }

                            OutlinedButton(
                                onClick = { showIntervalDialog = true },
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Text(
                                    stringResource(
                                        Res.string.monitoring_interval_minutes,
                                        currentInterval.value
                                    )
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            if (showIntervalDialog) {
                                IntervalPickerDialog(
                                    currentInterval = currentInterval.value,
                                    onDismiss = { showIntervalDialog = false },
                                    onIntervalSelected = { newInterval ->
                                        appPreferences.saveMonitoringInterval(newInterval)
                                        currentInterval.value = newInterval
                                        showIntervalDialog = false

                                        // Restart monitoring with new interval if user is logged in
                                        if (viewModel.isLoggedIn.value) {
                                            userSessionService.stopMonitoring()
                                            userSessionService.startMonitoring(newInterval)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                SettingItem(
                    title = stringResource(Res.string.change_password),
                    subtitle =stringResource(Res.string.change_password_desc),
                    onClick = { onNavigateToChangePassword()}
                )

                SettingItem(
                    title = stringResource(Res.string.app_permissions),
                    subtitle = stringResource(Res.string.app_permissions_desc),
                    onClick = { onNavigateToPermissions() }
                )

                SettingItem(
                    title = stringResource(Res.string.about),
                    subtitle = stringResource(Res.string.about_desc),
                    onClick = { onNavigateToAbout() }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            updateProfileState = updateProfileState,
            onDismiss = { showEditProfileDialog = false },
            onUpdateProfile = { name, division, phone ->
                viewModel.updateProfile(name, division, phone)
            }
        )
    }

    if (showLangDialog) {
        LanguagePickerDialog(onDismiss = { showLangDialog = false })
    }
}
