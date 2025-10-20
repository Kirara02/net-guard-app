package com.uniguard.netguard_app.presentation.ui.components

import androidx.compose.runtime.Composable

// Simple toast implementation for multiplatform
// This is a basic implementation - in a real app you might want to use platform-specific toast libraries

expect fun showToast(message: String, type: ToastType = ToastType.Info)

enum class ToastType {
    Success,
    Error,
    Info,
    Warning
}

// Extension functions for easy usage
fun showSuccessToast(message: String) = showToast(message, ToastType.Success)
fun showErrorToast(message: String) = showToast(message, ToastType.Error)
fun showInfoToast(message: String) = showToast(message, ToastType.Info)
fun showWarningToast(message: String) = showToast(message, ToastType.Warning)