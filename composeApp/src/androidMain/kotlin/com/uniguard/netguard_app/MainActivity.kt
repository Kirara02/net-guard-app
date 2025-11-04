package com.uniguard.netguard_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import com.uniguard.netguard_app.di.init
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            KoinApplication(application = {
                androidContext(context.applicationContext)
                init()
            }) {
                App()
            }
        }
    }
}
