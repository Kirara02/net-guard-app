package com.uniguard.netguard_app

import androidx.compose.runtime.*
import com.uniguard.netguard_app.presentation.navigation.AppNavigation
import com.uniguard.netguard_app.presentation.ui.theme.NetGuardTheme

@Composable
fun App() {
    LaunchedEffect(Unit) {
        log { "App started." }
    }

    NetGuardTheme {
        AppNavigation()
    }
}