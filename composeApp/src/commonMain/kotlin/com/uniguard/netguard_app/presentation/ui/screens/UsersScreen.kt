package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import com.uniguard.netguard_app.presentation.viewmodel.UserViewModel
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = rememberKoinViewModel<UserViewModel>(),
    authViewModel: AuthViewModel = rememberKoinViewModel<AuthViewModel>()
) {

    val usersState by viewModel.usersState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val createUserState by viewModel.createUserState.collectAsState()
    val updateUserState by viewModel.updateUserState.collectAsState()
    val deleteUserState by viewModel.deleteUserState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var showEditUserDialog by remember { mutableStateOf<User?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetAllState()
        }
    }

    // Handle create user success
    LaunchedEffect(createUserState) {
        if (createUserState is ApiResult.Success) {
            showAddUserDialog = false
            viewModel.resetCreateUserState()
        }
    }

    // Handle update user success
    LaunchedEffect(updateUserState) {
        if (updateUserState is ApiResult.Success) {
            showEditUserDialog = null
            viewModel.resetUpdateUserState()
        }
    }

    // Handle delete user success
    LaunchedEffect(deleteUserState) {
        if (deleteUserState is ApiResult.Success) {
            showDeleteConfirmDialog = null
            viewModel.resetDeleteUserState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(Res.string.users_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddUserDialog = true
                }
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = stringResource(Res.string.users_add))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
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
                            stringResource(Res.string.report_generating),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (usersState) {
                        is ApiResult.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                        is ApiResult.Success -> {
                            val users = (usersState as ApiResult.Success).data
                            if (users.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = stringResource(Res.string.report_no_reports),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(users) { user ->
                                    var showActionMenu by remember { mutableStateOf(false) }

                                    Row {
                                        ListItem(
                                            headlineContent = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(user.name)
                                                    currentUser?.let {
                                                        if (it.id == user.id) {
                                                            Text(
                                                                text = stringResource(Res.string.users_current_user),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            supportingContent = {
                                                Column {
                                                    Text(user.email)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(Res.string.users_role) + ":",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = if (user.role.lowercase() == "admin") stringResource(Res.string.users_role_admin) else stringResource(Res.string.users_role_user),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (user.role.lowercase() == "admin")
                                                                MaterialTheme.colorScheme.primary
                                                            else
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            },
                                            leadingContent = {
                                                val avatarUrl = remember(user.name) {
                                                    val encodedName = user.name.replace(" ", "%20")
                                                    "https://ui-avatars.com/api/?background=0D8ABC&color=fff&name=$encodedName"
                                                }

                                                AsyncImage(
                                                    model = avatarUrl,
                                                    contentDescription = "Profile Avatar",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape)
                                                )
                                            },
                                            trailingContent = {
                                                IconButton(
                                                    onClick = { showActionMenu = true },
                                                ) {
                                                    Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                                                }

                                                DropdownMenu(
                                                    expanded = showActionMenu,
                                                    onDismissRequest = { showActionMenu = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text(stringResource(Res.string.users_edit)) },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.users_edit))
                                                        },
                                                        onClick = {
                                                            showActionMenu = false
                                                            showEditUserDialog = user
                                                        }
                                                    )
                                                    currentUser?.let {
                                                        if(it.id != user.id) {
                                                            DropdownMenuItem(
                                                                text = { Text(stringResource(Res.string.users_delete)) },
                                                                leadingIcon = {
                                                                    Icon(Icons.Default.PersonRemove, contentDescription = stringResource(Res.string.users_delete))
                                                                },
                                                                onClick = {
                                                                    showActionMenu = false
                                                                    showDeleteConfirmDialog = user
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }

                            }
                        }

                        is ApiResult.Error -> {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = (usersState as ApiResult.Error).message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        is ApiResult.Initial -> {
                            // Show nothing initially
                        }
                    }
                }
            }
        }
    }

    // Add User Dialog
    if (showAddUserDialog) {
        UserFormDialog(
            title = "Add User",
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, email, password, division, phone, role ->
                viewModel.createUser(name, email, password, division, phone, role)
            },
            apiState = createUserState
        )
    }

    // Edit User Dialog
    showEditUserDialog?.let { user ->
        UserFormDialog(
            title = "Edit User",
            user = user,
            onDismiss = { showEditUserDialog = null },
            onConfirm = { name, email, password, division, phone, role ->
                viewModel.updateUser(user.id, name, email, password, division, phone, role)
            },
            apiState = updateUserState
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(stringResource(Res.string.users_delete)) },
            text = { Text(stringResource(Res.string.users_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user.id)
                    },
                    enabled = deleteUserState !is ApiResult.Loading
                ) {
                    Text(stringResource(Res.string.users_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(stringResource(Res.string.users_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserFormDialog(
    title: String,
    user: User? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, password: String?, division: String, phone: String, role: String) -> Unit,
    apiState: ApiResult<*>
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var division by remember { mutableStateOf(user?.division ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "User") }
    var expanded by remember { mutableStateOf(false) }
    var showPasswordField by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val roles = listOf("User", "Admin")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                } else if (user.role.lowercase() == "user") { // Show password field for editing USER role
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
                        onConfirm(name, email, passwordToUse, division, phone, role)
                    }
                },
                enabled = name.isNotBlank() && email.isNotBlank() &&
                         (user != null || (password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword)) &&
                         (user == null || user.role.lowercase() != "user" || !showPasswordField ||
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