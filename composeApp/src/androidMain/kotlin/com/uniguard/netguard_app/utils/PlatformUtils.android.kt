package com.uniguard.netguard_app.utils

import android.os.Environment
import android.widget.Toast
import com.uniguard.netguard_app.di.applicationContext
import java.io.File

actual fun saveFile(data: ByteArray, fileName: String) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        file.writeBytes(data)

        // Show notification or toast that file was saved
        Toast.makeText(applicationContext, "File saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(applicationContext, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
    }
}