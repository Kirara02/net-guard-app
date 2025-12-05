package com.uniguard.netguard_app.utils

fun formatRole(role: String): String {
    return role.replace("_", " ").uppercase()
}

fun getInitials(name: String, maxLength: Int = 2): String {
    val parts = name.trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }

    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> {
            parts[0]
                .take(maxLength)
                .uppercase()
        }
        else -> {
            "${parts[0].first()}${parts[1].first()}".uppercase()
        }
    }
}