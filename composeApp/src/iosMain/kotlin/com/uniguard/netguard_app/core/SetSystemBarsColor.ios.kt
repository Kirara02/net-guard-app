package com.uniguard.netguard_app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow

@Composable
actual fun SetSystemBarsColor(backgroundColor: Color, darkIcons: Boolean) {
    SideEffect {
        val window = UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
        window?.overrideUserInterfaceStyle = if (darkIcons) {
            UIUserInterfaceStyle.UIUserInterfaceStyleLight
        } else {
            UIUserInterfaceStyle.UIUserInterfaceStyleDark
        }
    }
}