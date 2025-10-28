package com.uniguard.netguard_app.localization


object SupportedLanguages {
    val languages = listOf(
        AppLanguage("en", "English"),
        AppLanguage("id", "Bahasa Indonesia"),
    )

    fun findByCode(code: String) =
        languages.firstOrNull { it.code == code } ?: languages.first()
}