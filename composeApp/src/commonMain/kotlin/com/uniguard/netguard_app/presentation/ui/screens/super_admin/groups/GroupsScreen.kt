package com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Group
import com.uniguard.netguard_app.domain.model.toInfo
import com.uniguard.netguard_app.presentation.ui.screens.admin.users.composables.UserFormDialog
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.composables.ConfirmDeleteDialog
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.composables.GroupEditorDialog
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.composables.GroupItemCard
import com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables.ServerFormDialog
import com.uniguard.netguard_app.presentation.viewmodel.admin.UserViewModel
import com.uniguard.netguard_app.presentation.viewmodel.super_admin.GroupViewModel
import com.uniguard.netguard_app.presentation.viewmodel.user.AuthViewModel
import com.uniguard.netguard_app.presentation.viewmodel.user.ServerViewModel
import netguardapp.composeapp.generated.resources.Res
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    authViewModel: AuthViewModel,
    viewModel: GroupViewModel = rememberKoinViewModel<GroupViewModel>(),
    userViewModel: UserViewModel = rememberKoinViewModel<UserViewModel>(),
    serverViewModel: ServerViewModel = rememberKoinViewModel<ServerViewModel>(),
    onNavigateBack: () -> Unit
) {
    val groupsState by viewModel.groupsState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val createGroupState by viewModel.createGroupState.collectAsState()
    val updateGroupState by viewModel.updateGroupState.collectAsState()
    val deleteGroupState by viewModel.deleteGroupState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val createUserState by userViewModel.createUserState.collectAsState()
    val createServerState by serverViewModel.createServerState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Group?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Group?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedGroupForAdd by remember { mutableStateOf<Group?>(null) }
    var showAddServerDialog by remember { mutableStateOf(false) }

    val groups = remember(groupsState) {
        (groupsState as? ApiResult.Success)
            ?.data
            ?.map { it.toInfo() }
            ?: emptyList()
    }

    // Load on start
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    // Reset ViewModel state when leaving
    DisposableEffect(Unit) {
        onDispose { viewModel.resetAllState() }
    }

    // Auto-close dialogs after success
    LaunchedEffect(createGroupState) {
        if (createGroupState is ApiResult.Success) {
            showAddDialog = false
            viewModel.resetCreateGroupState()
        }
    }

    LaunchedEffect(updateGroupState) {
        if (updateGroupState is ApiResult.Success) {
            showEditDialog = null
            viewModel.resetUpdateGroupState()
        }
    }

    LaunchedEffect(deleteGroupState) {
        if (deleteGroupState is ApiResult.Success) {
            showDeleteDialog = null
            viewModel.resetDeleteGroupState()
        }
    }

    LaunchedEffect(createUserState) {
        if (createUserState is ApiResult.Success) {
            showAddUserDialog = false
            selectedGroupForAdd = null
            userViewModel.resetCreateUserState()
            viewModel.loadGroups()
        }
    }

    LaunchedEffect(createServerState) {
        if (createServerState is ApiResult.Success) {
            showAddServerDialog = false
            selectedGroupForAdd = null
            serverViewModel.resetCreateServerState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.groups_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.resetAllState()
                        viewModel.loadGroups()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }

                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Add User")
                    }
                }
            )
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (isLoading) {
                LoadingColumn()
            } else {
                when (groupsState) {

                    is ApiResult.Success -> {
                        val groups = (groupsState as ApiResult.Success).data

                        if (groups.isEmpty()) {
                            EmptyState()
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(groups, key = { it.id }) { group ->
                                    GroupItemCard(
                                        group = group,
                                        onAddUser = { grp ->
                                            selectedGroupForAdd = grp
                                            showAddUserDialog = true
                                        },
                                        onAddServer = { grp ->
                                            selectedGroupForAdd = grp
                                            showAddServerDialog = true
                                        },
                                        onEdit = { showEditDialog = it },
                                        onDelete = { showDeleteDialog = it }
                                    )
                                }
                            }
                        }
                    }

                    is ApiResult.Error -> {
                        ErrorState((groupsState as ApiResult.Error).message)
                    }

                    ApiResult.Loading -> LoadingColumn()
                    ApiResult.Initial -> {}
                }
            }
        }

        if (showAddUserDialog) {
            UserFormDialog(
                title = stringResource(Res.string.users_create),
                user = null,
                currentUserRole = currentUser?.role ?: "user",
                groups = groupsState.let {
                    if (it is ApiResult.Success) it.data else emptyList()
                },
                onDismiss = {
                    showAddUserDialog = false
                },
                onConfirm = { name, email, password, division, phone, role, groupId ->
                    userViewModel.createUser(
                        name = name,
                        email = email,
                        password = password ?: "",
                        division = division,
                        phone = phone,
                        role = role,
                        groupId = groupId
                    )
                },
                apiState = createUserState,
                preSelectedGroup = selectedGroupForAdd
            )
        }
    }

    if (showAddServerDialog) {
        ServerFormDialog(
            title = stringResource(Res.string.server_management_add_title),
            server = null,
            currentUserRole = currentUser?.role ?: "user",
            preSelectedGroup = selectedGroupForAdd?.toInfo(),
            groups = groups,
            onConfirm = { name, url, groupId ->
                serverViewModel.addServer(name, url, groupId)
                showAddDialog = false
            },
            onDismiss = {
                showAddServerDialog = false
            }
        )
    }

    if (showAddDialog) {
        GroupEditorDialog(
            title = stringResource(Res.string.groups_add),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, max ->
                viewModel.createGroup(name, desc, max)
            }
        )
    }

    if (showEditDialog != null) {
        GroupEditorDialog(
            title = stringResource(Res.string.groups_edit),
            group = showEditDialog!!,
            onDismiss = { showEditDialog = null },
            onConfirm = { name, desc, max ->
                viewModel.updateGroup(showEditDialog!!.id, name, desc, max)
            }
        )
    }

    if (showDeleteDialog != null) {
        ConfirmDeleteDialog(
            group = showDeleteDialog!!,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteGroup(showDeleteDialog!!.id)
            }
        )
    }
}


@Composable
fun LoadingColumn() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(Res.string.groups_processing))
    }
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No groups found", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
