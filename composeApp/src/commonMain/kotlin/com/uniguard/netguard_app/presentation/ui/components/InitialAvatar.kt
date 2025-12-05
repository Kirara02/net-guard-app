package com.uniguard.netguard_app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniguard.netguard_app.utils.getInitials


private val avatarColors = listOf(
    Color(0xFFEF9A9A), // Red
    Color(0xFFF48FB1), // Pink
    Color(0xFFCE93D8), // Purple
    Color(0xFF9FA8DA), // Indigo
    Color(0xFF90CAF9), // Blue
    Color(0xFF81D4FA), // Light Blue
    Color(0xFF80DEEA), // Cyan
    Color(0xFF80CBC4), // Teal
    Color(0xFFA5D6A7), // Green
    Color(0xFFE6EE9C), // Lime
    Color(0xFFFFF59D), // Yellow
    Color(0xFFFFCC80), // Orange
    Color(0xFFBCAAA4)  // Brown
)

private fun pickAvatarColor(seed: String): Color {
    val index = kotlin.math.abs(seed.hashCode()) % avatarColors.size
    return avatarColors[index]
}

private fun getReadableTextColor(background: Color): Color {
    return if (background.luminance() < 0.5f) Color.White else Color.Black
}

@Composable
fun InitialAvatar(
    name: String,
    size: Dp = 46.dp
) {
    val initials = remember(name) {
        getInitials(name)
    }

    val bgColor = remember(name) {
        pickAvatarColor(name)
    }

    val textColor = remember(bgColor) {
        getReadableTextColor(bgColor)
    }

    // ✅ Auto-scale text based on avatar size
    val textSizeSp = remember(size) {
        (size.value * 0.45f).coerceIn(12f, 32f)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = textSizeSp.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1
        )
    }
}



