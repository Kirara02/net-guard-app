package com.uniguard.netguard_app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniguard.netguard_app.utils.formatRelativeTimeValue
import com.uniguard.netguard_app.utils.formatRole
import com.uniguard.netguard_app.utils.formatUtcToLocal
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

// Buttons
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun IncidentHistoryCard(
    serverName: String,
    status: String,
    timestamp: String,
    duration: String? = null,
    reportedBy: String,
    resolvedBy: String? = null,
    onResolveClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = serverName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${stringResource(Res.string.reported_by)}: $reportedBy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    resolvedBy?.let {
                        Text(
                            text = "${stringResource(Res.string.resolved_by)}: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                StatusIndicator(status = status)
            }

            duration?.let {
                Text(
                    text = "${stringResource(Res.string.duration)}: $duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            onResolveClick?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = it,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(Res.string.resolve))
                }
            }
        }
    }
}

// Text Field with Error
@Composable
fun OutlinedTextFieldWithError(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            isError = error != null,
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// Error Message Component
@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            onRetry?.let {
                TextButton(onClick = it) {
                    Text(stringResource(Res.string.retry))
                }
            }
        }
    }
}

// Status Indicator
@Composable
fun StatusIndicator(status: String) {
    val (color, _) = when (status.uppercase()) {
        "UP", "ONLINE", "RESOLVED" -> Color.Green to status
        "DOWN", "OFFLINE" -> Color.Red to status
        else -> Color.Gray to status
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
    )
}


@Composable
fun RoleBadge(role: String) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = formatRole(role),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.height(24.dp)
    )
}

@Composable
fun GroupBadge(groupName: String) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = groupName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        modifier = Modifier.height(24.dp)
    )
}

@Composable
fun relativeTimeText(timestamp: String): String {
    val (type, value) = formatRelativeTimeValue(timestamp)

    return when (type) {

        "JUST_NOW" ->
            stringResource(Res.string.time_just_now)

        "MINUTES" ->
            stringResource(Res.string.time_minute_ago, value)

        "HOURS" ->
            stringResource(Res.string.time_hour_ago, value)

        "DAYS" ->
            stringResource(Res.string.time_day_ago, value)

        else ->
            formatUtcToLocal(timestamp)
    }
}