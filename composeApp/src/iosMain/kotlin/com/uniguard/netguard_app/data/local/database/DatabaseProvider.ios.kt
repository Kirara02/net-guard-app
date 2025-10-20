package com.uniguard.netguard_app.data.local.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.uniguard.netguardapp.db.AppDatabase

actual class DatabaseProvider {
    actual fun getDatabase(): AppDatabase {
        val driver: SqlDriver = NativeSqliteDriver(
            schema = AppDatabase.Schema,
            name = "netguard.db"
        )
        return AppDatabase(driver)
    }
}