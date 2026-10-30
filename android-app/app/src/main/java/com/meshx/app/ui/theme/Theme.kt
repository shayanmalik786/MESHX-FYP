package com.meshx.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Same tokens as the Figma style guide.
object MeshColors {
    val Primary = Color(0xFF0F9D8A)
    val PrimaryDark = Color(0xFF0B6E61)
    val PrimarySoft = Color(0xFFD7F2EE)
    val Navy = Color(0xFF0B1F2A)
    val Bg = Color(0xFFF4F7F8)
    val Surface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF0E1A22)
    val Muted = Color(0xFF5E6B73)
    val Outline = Color(0xFFDCE3E7)
    val BubbleIn = Color(0xFFE9EEF1)
    val Success = Color(0xFF1E9E5A)
    val Warning = Color(0xFFE8A317)
    val Danger = Color(0xFFD64545)

    /** Colour for a link-quality score 0..1. */
    fun score(score: Float) = when {
        score >= 0.75f -> Success
        score >= 0.5f -> Warning
        else -> Danger
    }
}

private val scheme = lightColorScheme(
    primary = MeshColors.Primary,
    onPrimary = MeshColors.Surface,
    primaryContainer = MeshColors.PrimarySoft,
    onPrimaryContainer = MeshColors.PrimaryDark,
    secondaryContainer = MeshColors.PrimarySoft,
    onSecondaryContainer = MeshColors.PrimaryDark,
    background = MeshColors.Bg,
    onBackground = MeshColors.Ink,
    surface = MeshColors.Surface,
    onSurface = MeshColors.Ink,
    surfaceVariant = MeshColors.BubbleIn,
    onSurfaceVariant = MeshColors.Muted,
    outline = MeshColors.Outline,
    error = MeshColors.Danger,
)

@Composable
fun MeshXTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
