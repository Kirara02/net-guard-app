package com.uniguard.netguard_app.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun SetSystemBarsColor(
    backgroundColor: Color,
    darkIcons: Boolean
)