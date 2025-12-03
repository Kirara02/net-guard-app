package com.uniguard.netguard_app.presentation.ui.screens.user.servers.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.uniguard.netguard_app.domain.model.GroupInfo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerGroupFilterDropdown(
    groups: List<GroupInfo>,
    selectedGroupId: String?,
    onSelect: (String?) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    val selectedName =
        groups.firstOrNull { it.id == selectedGroupId }?.name
            ?: "All"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            label = { Text("Filter by group") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            // --- ALL GROUPS
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )

            // --- EACH GROUP
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group.name) },
                    onClick = {
                        onSelect(group.id)
                        expanded = false
                    }
                )
            }
        }
    }
}