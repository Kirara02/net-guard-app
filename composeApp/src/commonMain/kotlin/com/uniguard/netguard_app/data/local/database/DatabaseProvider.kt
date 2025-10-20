package com.uniguard.netguard_app.data.local.database

import com.uniguard.netguardapp.db.AppDatabase

expect class DatabaseProvider {
    fun getDatabase(): AppDatabase
}