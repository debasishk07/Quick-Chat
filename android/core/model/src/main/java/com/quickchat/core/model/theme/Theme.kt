package com.quickchat.core.model.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 1. Color Palette (Anthropic-inspired warm cream & charcoal with terracotta accent)
val CreamBackground = Color(0xFFF5F0E8)
val CharcoalText = Color(0xFF1F1B16)
val CreamSurface = Color(0xFFFAF6F0)
val TerracottaAccent = Color(0xFFD97757)
val WarmGrey = Color(0xFFE4DDD3)

val DarkBackground = Color(0xFF161513)
val DarkText = Color(0xFFEAE3D8)
val DarkSurface = Color(0xFF22201D)
val DarkWarmGrey = Color(0xFF35322E)

private val LightColorScheme = lightColorScheme(
    primary = CharcoalText,
    onPrimary = CreamBackground,
    secondary = TerracottaAccent,
    onSecondary = Color.White,
    tertiary = TerracottaAccent,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = CharcoalText,
    surface = CreamSurface,
    onSurface = CharcoalText,
    outline = CharcoalText.copy(alpha = 0.5f),
    surfaceVariant = WarmGrey
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkText,
    onPrimary = DarkBackground,
    secondary = TerracottaAccent,
    onSecondary = Color.White,
    tertiary = TerracottaAccent,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    outline = DarkText.copy(alpha = 0.5f),
    surfaceVariant = DarkWarmGrey
)

// Custom structural theme parameters (handy for custom views)
data class SketchyColors(
    val background: Color,
    val text: Color,
    val surface: Color,
    val accent: Color,
    val border: Color,
    val isDark: Boolean
)

val LocalSketchyColors = staticCompositionLocalOf {
    SketchyColors(
        background = CreamBackground,
        text = CharcoalText,
        surface = CreamSurface,
        accent = TerracottaAccent,
        border = CharcoalText,
        isDark = false
    )
}

// 2. Typography pairing (Serif for headers, Sans-Serif for body/chrome)
val SketchyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun SketchyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val sketchyColors = if (darkTheme) {
        SketchyColors(
            background = DarkBackground,
            text = DarkText,
            surface = DarkSurface,
            accent = TerracottaAccent,
            border = DarkText,
            isDark = true
        )
    } else {
        SketchyColors(
            background = CreamBackground,
            text = CharcoalText,
            surface = CreamSurface,
            accent = TerracottaAccent,
            border = CharcoalText,
            isDark = false
        )
    }

    CompositionLocalProvider(
        LocalSketchyColors provides sketchyColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SketchyTypography,
            content = content
        )
    }
}
