package com.uniguard.netguard_app.presentation.ui.components

import android.widget.Toast
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

