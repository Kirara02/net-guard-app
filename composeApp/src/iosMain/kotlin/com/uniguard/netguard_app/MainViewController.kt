package com.uniguard.netguard_app

import androidx.compose.ui.window.ComposeUIViewController
import com.uniguard.netguard_app.di.init
import org.koin.compose.KoinApplication

fun MainViewController() = ComposeUIViewController {
    KoinApplication(application = {
        init()
    }) {
        App()
    }
}