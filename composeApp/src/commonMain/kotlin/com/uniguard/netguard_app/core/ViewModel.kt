package com.uniguard.netguard_app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.currentKoinScope
import org.koin.core.scope.Scope

@Composable
inline fun <reified T : Any> rememberKoinViewModel(): T {
    val koinScope: Scope = currentKoinScope()
    return remember { koinScope.get<T>() }
}