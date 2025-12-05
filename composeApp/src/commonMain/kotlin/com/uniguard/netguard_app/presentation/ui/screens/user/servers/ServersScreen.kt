package com.uniguard.netguard_app.presentation.ui.screens.user.servers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.domain.model.toInfo
import com.uniguard.netguard_app.presentation.ui.components.showErrorToast
import com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables.ServerAdminCard
import com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables.ServerCard
import com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables.ServerFormDialog
import com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables.ServerGroupFilterDropdown
import com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables.ServerOverviewCard
import com.uniguard.netguard_app.presentation.viewmodel.super_admin.GroupViewModel
import com.uniguard.netguard_app.presentation.viewmodel.user.ServerViewModel
import com.uniguard.netguardapp.db.ServerStatusEntity
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    viewModel: ServerViewModel = rememberKoinViewModel<ServerViewModel>(),
    groupViewModel: GroupViewModel = rememberKoinViewModel<GroupViewModel>(),
    onNavigateBack: () -> Unit
) {
    val servers by viewModel.servers.collectAsState()
    val serverStatuses by viewModel.serverStatuses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val groupsState by groupViewModel.groupsState.collectAsState()

    val createState by viewModel.createServerState.collectAsState()
    val updateState by viewModel.updateServerState.collectAsState()
    val deleteState by viewModel.deleteServerState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    val filteredServers = remember(servers, selectedGroupId) {
        if (selectedGroupId == null) servers
        else servers.filter { it.group.id == selectedGroupId }
    }

    val groups = remember(groupsState) {
        (groupsState as? ApiResult.Success)
            ?.data
            ?.map { it.toInfo() }
            ?: emptyList()
    }

    val isSuperAdmin = viewModel.isSuperAdmin()
    val isUser = viewModel.isUser()

    LaunchedEffect(Unit) {
        viewModel.loadServersByRole()
    }

    LaunchedEffect(Unit) {
        if (isSuperAdmin) {
            groupViewModel.loadGroups()
        }
    }

    LaunchedEffect(createState) {
        if (createState is ApiResult.Success) {
            showAddDialog = false
            viewModel.resetCreateServerState()
            viewModel.loadServersByRole()
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is ApiResult.Success) {
            showEditDialog = false
            viewModel.selectServer(null)
            viewModel.resetUpdateServerState()
            viewModel.loadServersByRole()
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState is ApiResult.Success) {
            showDeleteDialog = false
            viewModel.selectServer(null)
            viewModel.resetDeleteServerState()
            viewModel.loadServersByRole()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.server_management_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadServersByRole() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.refresh))
                    }
                    AnimatedVisibility(
                        visible = !isUser
                    ) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(Res.string.server_management_add_server),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && servers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(stringResource(Res.string.server_management_loading))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    if (isSuperAdmin) {

                        item {
                            ServerGroupFilterDropdown(
                                groups = groups,
                                selectedGroupId = selectedGroupId,
                                onSelect = { selectedGroupId = it }
                            )
                        }

                        items(filteredServers) { server ->
                            ServerAdminCard(
                                server = server,
                                onEdit = {
                                    viewModel.selectServer(server)
                                    showEditDialog = true
                                },
                                onDelete = {
                                    viewModel.selectServer(server)
                                    showDeleteDialog = true
                                }
                            )
                        }

                    } else {

                        item {
                            ServerOverviewCard(
                                total = viewModel.totalServers,
                                online = serverStatuses.values.count { it.status == "UP" },
                                offline = serverStatuses.values.count { it.status == "DOWN" },
                                unknown = viewModel.totalServers - serverStatuses.size
                            )
                        }

                        adminUserServerSections(
                            servers = servers,
                            statuses = serverStatuses,
                            canManage = !isUser,
                            onEdit = { server ->
                                viewModel.selectServer(server)
                                showEditDialog = true
                            },
                            onDelete = { server ->
                                viewModel.selectServer(server)
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Loading overlay
            if (isLoading && servers.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text(stringResource(Res.string.server_management_syncing))
                        }
                    }
                }
            }
        }
    }

    // Add Server Dialog
    if (showAddDialog) {
        ServerFormDialog(
            title = stringResource(Res.string.server_management_add_title),
            server = null,
            currentUserRole = if (isSuperAdmin) "SUPER_ADMIN" else "USER",
            groups = groups,
            onConfirm = { name, url, groupId ->
                viewModel.addServer(name, url, groupId)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showEditDialog && selectedServer != null) {
        ServerFormDialog(
            title = stringResource(Res.string.server_management_edit_title),
            server = selectedServer,
            currentUserRole = if (isSuperAdmin) "SUPER_ADMIN" else "USER",
            groups = groups,
            preSelectedGroup = selectedServer!!.group,
            onConfirm = { name, url, groupId ->
                viewModel.updateServer(selectedServer!!.id, name, url, groupId)
            },
            onDismiss = {
                showEditDialog = false
                viewModel.selectServer(null)
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedServer != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                viewModel.selectServer(null)
            },
            title = { Text(stringResource(Res.string.server_management_delete_title)) },
            text = {
                Text(stringResource(Res.string.server_management_delete_confirm, selectedServer!!.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteServer(selectedServer!!.id)
                        showDeleteDialog = false
                        viewModel.selectServer(null)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(Res.string.server_management_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.selectServer(null)
                }) {
                    Text(stringResource(Res.string.server_management_cancel))
                }
            }
        )
    }
}

fun LazyListScope.adminUserServerSections(
    servers: List<Server>,
    statuses: Map<String, ServerStatusEntity>,
    canManage: Boolean,
    onEdit: (Server) -> Unit,
    onDelete: (Server) -> Unit
) {
    val online = servers.filter { statuses[it.id]?.status == "UP" }
    val offline = servers.filter { statuses[it.id]?.status == "DOWN" }
    val unknown = servers.filter { statuses[it.id]?.status !in listOf("UP", "DOWN") }

    fun renderGroup(title: String, color: Color, list: List<Server>) {
        if (list.isEmpty()) return

        item {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = color
            )
        }

        items(list) { server ->
            ServerCard(
                server = server,
                serverStatus = statuses[server.id],
                canManage = canManage,
                onEdit = { onEdit(server) },
                onDelete = { onDelete(server) }
            )
        }
    }

    renderGroup("Online", Color.Green, online)
    renderGroup("Offline", Color.Red, offline)
    renderGroup("Unknown", Color.Gray, unknown)
}