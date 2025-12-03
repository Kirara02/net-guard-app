package com.uniguard.netguard_app.presentation.ui.screens.admin.users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.model.UserRole
import com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables.ConfirmDeleteDialog
import com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables.EmptyUserSection
import com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables.UserFormDialog
import com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables.UserList
import com.uniguard.netguard_app.presentation.viewmodel.user.AuthViewModel
import com.uniguard.netguard_app.presentation.viewmodel.admin.UserViewModel
import com.uniguard.netguard_app.presentation.viewmodel.super_admin.GroupViewModel
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = rememberKoinViewModel<UserViewModel>(),
    groupViewModel: GroupViewModel = rememberKoinViewModel<GroupViewModel>(),
    authViewModel: AuthViewModel
) {

    val groupsState by groupViewModel.groupsState.collectAsState()
    val usersState by viewModel.usersState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val createState by viewModel.createUserState.collectAsState()
    val updateState by viewModel.updateUserState.collectAsState()
    val deleteState by viewModel.deleteUserState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var selectedGroupFilter by remember { mutableStateOf("ALL") }

    var showAddDialog by remember { mutableStateOf(false) }
    var editUser by remember { mutableStateOf<User?>(null) }
    var deleteUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    LaunchedEffect(Unit) {
        if (currentUser?.userRole == UserRole.SUPER_ADMIN) {
            groupViewModel.loadGroups()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.users_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add User")
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> LoadingSection()

                usersState is ApiResult.Error -> ErrorSection((usersState as ApiResult.Error).message)

                usersState is ApiResult.Success -> {
                    val users = (usersState as ApiResult.Success).data

                    val filteredUsers = users
                        .filter { user ->
                            val roleMatch = when (selectedRoleFilter) {
                                "ALL" -> true
                                else -> user.userRole.name == selectedRoleFilter
                            }

                            val groupMatch = when (selectedGroupFilter) {
                                "ALL" -> true
                                else -> user.group?.name == selectedGroupFilter
                            }

                            roleMatch && groupMatch
                        }


                    if (users.isEmpty()) EmptyUserSection()
                    else UserList(
                        users = filteredUsers,
                        currentUser = currentUser,
                        selectedRole = selectedRoleFilter,
                        selectedGroup = selectedGroupFilter,
                        groups = (groupsState as? ApiResult.Success)?.data ?: emptyList(),
                        onRoleChange = { selectedRoleFilter = it },
                        onGroupChange = { selectedGroupFilter = it },
                        onEdit = { editUser = it },
                        onDelete = { deleteUser = it }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        UserFormDialog(
            title = "Add User",
            currentUserRole = currentUser?.role ?: "user",
            groups = (groupsState as? ApiResult.Success)?.data ?: emptyList(),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email, password, division, phone, role, groupId ->
                viewModel.createUser(name, email, password, division, phone, role, groupId)
                showAddDialog = false
            },
            apiState = createState
        )
    }

    editUser?.let { user ->
        UserFormDialog(
            title = "Edit User",
            user = user,
            currentUserRole = currentUser?.role ?: "user",
            groups = (groupsState as? ApiResult.Success)?.data ?: emptyList(),
            onDismiss = { editUser = null },
            onConfirm = { name, email, password, division, phone, role, groupId ->
                viewModel.updateUser(user.id, name, email, password, division, phone, role, groupId)
                editUser = null
            },
            apiState = updateState
        )
    }

    deleteUser?.let { user ->
        ConfirmDeleteDialog(
            onDismiss = { deleteUser = null },
            onConfirm = { viewModel.deleteUser(user.id) },
            isProcessing = deleteState is ApiResult.Loading
        )
    }
}


@Composable
private fun LoadingSection() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Loading...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ErrorSection(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}