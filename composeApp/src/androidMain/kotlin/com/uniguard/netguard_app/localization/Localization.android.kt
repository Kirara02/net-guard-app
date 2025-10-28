package com.uniguard.netguard_app.localization

import android.os.LocaleList
import com.uniguard.netguard_app.di.applicationContext
import java.util.Locale

actual class Localization {
    actual fun applyLanguage(iso: String) {
        val locale: Locale = Locale.Builder().setLanguage(iso).build()
        Locale.setDefault(locale)
        val config = applicationContext.resources.configuration
        config.setLocales(LocaleList(locale))
    }
}