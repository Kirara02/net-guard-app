package com.uniguard.netguard_app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.uniguard.netguard_app.di.initializeAppContext

class NetGuardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init()
        initializeAppContext(this)
        FirebaseApp.initializeApp(this)
    }
}