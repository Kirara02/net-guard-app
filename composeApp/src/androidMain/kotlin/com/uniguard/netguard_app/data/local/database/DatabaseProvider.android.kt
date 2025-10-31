package com.uniguard.netguard_app.data.local.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.uniguard.netguard_app.BuildKonfig
import com.uniguard.netguardapp.db.AppDatabase

actual class DatabaseProvider(private val context: Context) {
    actual fun getDatabase(): AppDatabase {
        val driver: SqlDriver = AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = BuildKonfig.DB_NAME
        )
        return AppDatabase(driver)
    }
}