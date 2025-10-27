package com.uniguard.netguard_app

import androidx.compose.runtime.*
import com.uniguard.netguard_app.data.local.preferences.AuthPreferences
import com.uniguard.netguard_app.di.getKoinInstance
import com.uniguard.netguard_app.presentation.navigation.AppNavigation
import com.uniguard.netguard_app.presentation.ui.theme.NetGuardTheme

@Composable
fun App() {
    val authPreferences = getKoinInstance<AuthPreferences>()
    val isDarkTheme by authPreferences.themePreferenceFlow.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        log { "App started." }
    }

    NetGuardTheme(darkTheme = isDarkTheme) {
        AppNavigation()
    }
}