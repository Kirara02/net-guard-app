package com.uniguard.netguard_app

import android.app.Application
import com.uniguard.netguard_app.di.initializeAppContext

class NetGuardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeAppContext(this)
    }
}