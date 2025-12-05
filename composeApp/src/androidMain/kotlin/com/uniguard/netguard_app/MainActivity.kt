package com.uniguard.netguard_app

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.uniguard.netguard_app.di.init
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var appUpdateManager: AppUpdateManager

    private val updateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showCompletedUpdateDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        setContent {
            val context = LocalContext.current
            KoinApplication(application = {
                androidContext(context.applicationContext)
                init()
            }) {

                LaunchedEffect(Unit) {
                    checkForAppUpdate()
                }

                App()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showCompletedUpdateDialog()
            }
        }
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->

            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showCompletedUpdateDialog()
                return@addOnSuccessListener
            }

            val isAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (isAvailable && flexibleAllowed) {
                appUpdateManager.registerListener(updateListener)

                val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()

                try {
                    appUpdateManager.startUpdateFlow(info, this, options)
                } catch (e: Exception) {
                    log { "Failed to start update flow: ${e.message}" }
                }
            }
        }.addOnFailureListener { e ->
            log { "Failed to check for update: ${e.message}" }
        }
    }

    private fun showCompletedUpdateDialog() {
        runOnUiThread {
            if (!isFinishing) {
                AlertDialog.Builder(this)
                    .setTitle("Update Ready")
                    .setMessage("A new version has been downloaded. Restart the app to apply changes.")
                    .setCancelable(false)
                    .setPositiveButton("Restart") { _, _ ->
                        appUpdateManager.completeUpdate()
                    }
                    .setNegativeButton("Later", null)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        appUpdateManager.unregisterListener(updateListener)
    }
}
