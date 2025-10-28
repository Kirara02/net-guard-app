package com.uniguard.netguard_app

import androidx.compose.runtime.*
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.di.getKoinInstance
import com.uniguard.netguard_app.localization.Localization
import com.uniguard.netguard_app.presentation.navigation.AppNavigation
import com.uniguard.netguard_app.presentation.ui.theme.NetGuardTheme

@Composable
fun App() {
    val prefs = getKoinInstance<AppPreferences>()
    val locale = getKoinInstance<Localization>()

    val isDark by prefs.themePreferenceFlow.collectAsState(initial = false)
    val langCode by prefs.languageFlow.collectAsState(initial = "en")

    SideEffect {
        locale.applyLanguage(langCode)
    }

    LaunchedEffect(Unit) {
        log { "App started." }
    }

    NetGuardTheme(darkTheme = isDark) {
        AppNavigation()
    }

}