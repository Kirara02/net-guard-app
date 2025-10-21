package com.uniguard.netguard_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.uniguard.netguard_app.core.PermissionManager
import com.uniguard.netguard_app.di.init
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        PermissionManager(this).requestNotificationPermission()

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

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}