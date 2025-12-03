package com.uniguard.netguard_app.presentation.ui.screens.shared.settings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.di.getKoinInstance
import com.uniguard.netguard_app.localization.Localization
import com.uniguard.netguard_app.localization.SupportedLanguages
import netguardapp.composeapp.generated.resources.Res
import netguardapp.composeapp.generated.resources.cancel
import netguardapp.composeapp.generated.resources.ok
import netguardapp.composeapp.generated.resources.select_language
import org.jetbrains.compose.resources.stringResource

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