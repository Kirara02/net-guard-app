package com.uniguard.netguard_app.presentation.ui.screens.user.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.ReportParams
import com.uniguard.netguard_app.domain.model.ReportType
import com.uniguard.netguard_app.domain.model.ServerStatus
import com.uniguard.netguard_app.presentation.ui.components.*
import com.uniguard.netguard_app.presentation.viewmodel.user.ReportViewModel
import com.uniguard.netguard_app.utils.formatRelativeTime
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.LocalDate
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.graphics.vector.ImageVector
import com.uniguard.netguard_app.utils.getCurrentTimestamp
import com.uniguard.netguard_app.utils.saveFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = rememberKoinViewModel<ReportViewModel>(),
    onNavigateBack: () -> Unit
) {
    val reportsState by viewModel.reportsState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val serverOptions by viewModel.servers.collectAsState()

    // Filter states
    var selectedStatus by remember { mutableStateOf<ServerStatus?>(null) }
    var serverNameFilter by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Track last export format for filename generation
    var lastExportFormat by remember { mutableStateOf<ReportType?>(null) }

    // Handle export success
    LaunchedEffect(exportState) {
        if (exportState is ApiResult.Success) {
            val fileData = (exportState as ApiResult.Success).data
            val fileExtension = when (lastExportFormat) {
                ReportType.PDF -> "pdf"
                ReportType.EXCEL -> "xlsx"
                null -> "xlsx" // Default fallback
            }

            // Generate filename based on date filters
            val datePart = when {
                startDate != null && endDate != null -> {
                    // Both dates: use period format
                    "${startDate}_to_${endDate}".replace("-", "")
                }
                startDate != null -> {
                    // Only start date: use single date
                    startDate.toString().replace("-", "")
                }
                endDate != null -> {
                    // Only end date: use single date
                    endDate.toString().replace("-", "")
                }
                else -> {
                    // No dates: use timestamp
                    getCurrentTimestamp().substring(0, 19).replace(":", "").replace("-", "").replace("T", "_").replace(".", "")
                }
            }

            val fileName = "server_report_${datePart}.${fileExtension}"
            saveFile(fileData, fileName)
            viewModel.resetExportState()
        }
    }

    // Load reports when screen opens or filters change
    LaunchedEffect(selectedStatus, serverNameFilter, startDate, endDate) {
        val params = ReportParams(
            status = selectedStatus,
            serverName = serverNameFilter.takeIf { it.isNotBlank() },
            startDate = startDate?.toString(),
            endDate = endDate?.toString()
        )
        viewModel.loadReports(params)
    }

    // Load servers for dropdown options
    LaunchedEffect(Unit) {
        viewModel.loadServers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = stringResource(Res.string.report_icon_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(Res.string.report_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showExportMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(Res.string.report_export))
                    }

                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.report_export_pdf)) },
                            onClick = {
                                lastExportFormat = ReportType.PDF
                                val params = ReportParams(
                                    status = selectedStatus,
                                    serverName = serverNameFilter.takeIf { it.isNotBlank() },
                                    startDate = startDate?.toString(),
                                    endDate = endDate?.toString(),
                                    format = ReportType.PDF
                                )
                                viewModel.exportReports(params)
                                showExportMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.report_export_excel)) },
                            onClick = {
                                lastExportFormat = ReportType.EXCEL
                                val params = ReportParams(
                                    status = selectedStatus,
                                    serverName = serverNameFilter.takeIf { it.isNotBlank() },
                                    startDate = startDate?.toString(),
                                    endDate = endDate?.toString(),
                                    format = ReportType.EXCEL
                                )
                                viewModel.exportReports(params)
                                showExportMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.TableChart, contentDescription = null)
                            }
                        )
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filters
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    stringResource(Res.string.report_filters),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )

                                // Status Filter
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(Res.string.report_status), modifier = Modifier.weight(1f))
                                    FilterChip(
                                        selected = selectedStatus == null,
                                        onClick = { selectedStatus = null },
                                        label = { Text(stringResource(Res.string.report_all)) }
                                    )
                                    FilterChip(
                                        selected = selectedStatus == ServerStatus.DOWN,
                                        onClick = { selectedStatus = ServerStatus.DOWN },
                                        label = { Text(stringResource(Res.string.report_down)) }
                                    )
                                    FilterChip(
                                        selected = selectedStatus == ServerStatus.RESOLVED,
                                        onClick = { selectedStatus = ServerStatus.RESOLVED },
                                        label = { Text(stringResource(Res.string.report_resolved)) }
                                    )
                                }

                                // Server Name Filter
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = serverNameFilter.ifBlank { stringResource(Res.string.report_all_servers) },
                                        onValueChange = { serverNameFilter = it },
                                        label = { Text(stringResource(Res.string.report_server_name)) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                                        singleLine = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        },
                                        readOnly = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(Res.string.report_all_servers)) },
                                            onClick = {
                                                serverNameFilter = ""
                                                expanded = false
                                            }
                                        )
                                        serverOptions.forEach { server ->
                                            DropdownMenuItem(
                                                text = { Text(server.name) },
                                                onClick = {
                                                    serverNameFilter = server.name
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Date Range
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showStartDatePicker = true },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(startDate?.toString() ?: stringResource(Res.string.report_start_date))
                                    }
                                    OutlinedButton(
                                        onClick = { showEndDatePicker = true },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(endDate?.toString() ?: stringResource(Res.string.report_end_date))
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedStatus = null
                                            serverNameFilter = ""
                                            startDate = null
                                            endDate = null
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = stringResource(Res.string.report_reset_filters),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Report Summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    stringResource(Res.string.report_summary),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                // Show active filters
                                val activeFilters = mutableListOf<String>()
                                selectedStatus?.let { activeFilters.add("${stringResource(Res.string.report_status)} ${it.name}") }
                                if (serverNameFilter.isNotBlank()) {
                                    activeFilters.add("Server: $serverNameFilter")
                                }
                                if (startDate != null || endDate != null) {
                                    val period = when {
                                        startDate != null && endDate != null -> "$startDate - $endDate"
                                        startDate != null -> "From $startDate"
                                        endDate != null -> "Until $endDate"
                                        else -> ""
                                    }
                                    activeFilters.add("Period: $period")
                                }

                                if (activeFilters.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Filters: ${activeFilters.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                when (reportsState) {
                                    is ApiResult.Success -> {
                                        val reports = (reportsState as ApiResult.Success).data
                                        val totalReports = reports.size
                                        val downReports =
                                            reports.count { it.status.lowercase() == "down" }
                                        val resolvedReports =
                                            reports.count { it.status.lowercase() == "resolved" }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            ReportStatItem(
                                                label = stringResource(Res.string.report_total_reports),
                                                value = totalReports.toString(),
                                                icon = Icons.Default.Assessment
                                            )
                                            ReportStatItem(
                                                label = stringResource(Res.string.report_down_incidents),
                                                value = downReports.toString(),
                                                icon = Icons.Default.Warning
                                            )
                                            ReportStatItem(
                                                label = stringResource(Res.string.report_resolved_reports),
                                                value = resolvedReports.toString(),
                                                icon = Icons.Default.CheckCircle
                                            )
                                        }
                                    }

                                    else -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            ReportStatItem(
                                                label =  stringResource(Res.string.report_total_reports),
                                                value = "0",
                                                icon = Icons.Default.Assessment
                                            )
                                            ReportStatItem(
                                                label = stringResource(Res.string.report_down_incidents),
                                                value = "0",
                                                icon = Icons.Default.Warning
                                            )
                                            ReportStatItem(
                                                label =  stringResource(Res.string.report_resolved_reports),
                                                value = "0",
                                                icon = Icons.Default.CheckCircle
                                            )
                                        }
                                    }
                                }
                                // Date Picker Dialogs
                                if (showStartDatePicker) {
                                    val datePickerState = rememberDatePickerState()
                                    DatePickerDialog(
                                        onDismissRequest = { showStartDatePicker = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    startDate = LocalDate.fromEpochDays((millis / (24 * 60 * 60 * 1000)).toInt())
                                                }
                                                showStartDatePicker = false
                                            }) {
                                                Text(stringResource(Res.string.report_ok))
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showStartDatePicker = false }) {
                                                Text(stringResource(Res.string.report_cancel))
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }

                                if (showEndDatePicker) {
                                    val datePickerState = rememberDatePickerState()
                                    DatePickerDialog(
                                        onDismissRequest = { showEndDatePicker = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    endDate = LocalDate.fromEpochDays((millis / (24 * 60 * 60 * 1000)).toInt())
                                                }
                                                showEndDatePicker = false
                                            }) {
                                                Text(stringResource(Res.string.report_ok))
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showEndDatePicker = false }) {
                                                Text(stringResource(Res.string.report_cancel))
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }
                            }
                        }
                    }

                    // Server Status Report
                    item {
                        Text(
                            text = stringResource(Res.string.report_server_status),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Recent Incidents Report
                    item {
                        Text(
                            text = stringResource(Res.string.report_recent_incidents),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    when (reportsState) {
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
                            val reports = (reportsState as ApiResult.Success).data
                            if (reports.isEmpty()) {
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
                                items(reports) { report ->
                                    EnhancedIncidentCard(
                                        serverName = report.serverName,
                                        status = report.status,
                                        timestamp = formatRelativeTime(report.timestamp),
                                        url = report.url
                                    )
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
                                            text = (reportsState as ApiResult.Error).message,
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
}

@Composable
private fun ReportStatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}