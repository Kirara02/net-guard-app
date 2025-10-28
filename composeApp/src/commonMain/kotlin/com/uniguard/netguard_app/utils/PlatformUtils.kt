package com.uniguard.netguard_app.utils

expect fun getPlatformApiUrl(): String
expect fun saveFile(data: ByteArray, fileName: String)

