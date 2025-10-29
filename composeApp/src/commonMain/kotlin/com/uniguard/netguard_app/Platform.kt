package com.uniguard.netguard_app

interface Platform {
    val name: String
}

data class AppInfo(
    val name: String,
    val version: String
)

expect fun getAppInfo(): AppInfo