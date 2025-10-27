package com.uniguard.netguard_app.core

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun SetSystemBarsColor(backgroundColor: Color, darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        // The following lines are deprecated and will have no effect on Android 15+
        // window.statusBarColor = backgroundColor.toArgb()
        // window.navigationBarColor = backgroundColor.toArgb()

        // This remains the correct way to control system bar icon colors
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = darkIcons
            isAppearanceLightNavigationBars = darkIcons
        }
    }
}