package com.uniguard.netguard_app.presentation.ui.components

import androidx.compose.runtime.Composable
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIApplication

actual fun showToast(message: String, type: ToastType) {
    // For iOS, use UIAlertController for simple alerts
    val alertController = UIAlertController.alertControllerWithTitle(
        title = when (type) {
            ToastType.Success -> "Success"
            ToastType.Error -> "Error"
            ToastType.Info -> "Info"
            ToastType.Warning -> "Warning"
        },
        message = message,
        preferredStyle = platform.UIKit.UIAlertControllerStyleAlert
    )

    val okAction = UIAlertAction.actionWithTitle(
        title = "OK",
        style = platform.UIKit.UIAlertActionStyleDefault,
        handler = null
    )

    alertController.addAction(okAction)

    // Get the current view controller and present the alert
    val keyWindow = UIApplication.sharedApplication.keyWindow
    val rootViewController = keyWindow?.rootViewController
    rootViewController?.presentViewController(alertController, animated = true, completion = null)
}
