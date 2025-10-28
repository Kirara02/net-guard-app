package com.uniguard.netguard_app.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentInteractionController

actual fun getPlatformApiUrl(): String = Constants.API_BASE_URL_IOS

@OptIn(ExperimentalForeignApi::class)
actual fun saveFile(data: ByteArray, fileName: String) {
    try {
        val fileManager = NSFileManager.defaultManager()
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentsURL = urls.firstOrNull() as? NSURL

        if (documentsURL != null) {
            val fileURL = documentsURL.URLByAppendingPathComponent(fileName)!!
            val nsData = data.toNSData()

            nsData?.writeToURL(fileURL, true)

            // Show share sheet for iOS
            val application = UIApplication.sharedApplication()
            val rootViewController = application.keyWindow?.rootViewController

            if (rootViewController != null) {
                val documentInteractionController = UIDocumentInteractionController.interactionControllerWithURL(fileURL)
                documentInteractionController.presentOptionsMenuFromRect(
                    rect = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0),
                    inView = rootViewController.view,
                    animated = true
                )
            }
        }
    } catch (e: Exception) {
        // Handle error silently
        println("Failed to save file on iOS: ${e.message}")
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData? {
    return this.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
    }
}