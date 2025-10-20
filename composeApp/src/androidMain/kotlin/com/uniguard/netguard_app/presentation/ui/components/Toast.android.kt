package com.uniguard.netguard_app.presentation.ui.components

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.uniguard.netguard_app.di.applicationContext

actual fun showToast(message: String, type: ToastType) {
    // For Android, use Android Toast
    val context = applicationContext

    val duration = when (type) {
        ToastType.Error -> Toast.LENGTH_LONG
        else -> Toast.LENGTH_SHORT
    }
    Toast.makeText(context, message, duration).show()
}

