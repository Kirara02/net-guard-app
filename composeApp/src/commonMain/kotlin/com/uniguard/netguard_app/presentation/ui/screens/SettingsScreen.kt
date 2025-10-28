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
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.di.getKoinInstance
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.localization.Localization
import com.uniguard.netguard_app.localization.SupportedLanguages
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AuthViewModel = rememberKoinViewModel<AuthViewModel>(),
    onNavigateBack: () -> Unit
) {
    val userProfileState by viewModel.userProfileState.collectAsState()
    val updateProfileState by viewModel.updateProfileState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val appPreferences = getKoinInstance<AppPreferences>()


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

                SettingsItem(
                    title = stringResource(Res.string.change_password),
                    subtitle =stringResource(Res.string.change_password_desc),
                    onClick = { /* TODO */ }
                )

                SettingsItem(
                    title = stringResource(Res.string.about),
                    subtitle =stringResource(Res.string.about_desc),
                    onClick = { /* TODO */ }
                )
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
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
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

@Composable
fun LanguagePickerDialog(
    onDismiss: () -> Unit
) {
    val prefs = getKoinInstance<AppPreferences>()
    val locale = getKoinInstance<Localization>()
    val currentLang by prefs.languageFlow.collectAsState(initial = "en")
    var temp by remember(currentLang) { mutableStateOf(currentLang) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.select_language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SupportedLanguages.languages.forEach { lang ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = temp == lang.code,
                            onClick = { temp = lang.code }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(lang.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                prefs.saveLanguage(temp)
                locale.applyLanguage(temp)
                onDismiss()
            }) {
                Text(stringResource(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
