package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FengShiDarkColorScheme = darkColorScheme(
    primary = ImperialGold,
    onPrimary = OledBlack,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = ImperialGoldLight,
    secondary = VermillionRed,
    onSecondary = Color.White,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark
)

private val FengShiLightColorScheme = lightColorScheme(
    primary = EditorialVermillion,
    onPrimary = Color.White,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = EditorialVermillion,
    secondary = ImperialGold,
    onSecondary = Color.White,
    background = PaperLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> darkTheme
    }

    val colors = if (isDark) FengShiDarkColorScheme else FengShiLightColorScheme
    val fengShiColors = if (isDark) DarkThemeColors else LightThemeColors

    androidx.compose.runtime.CompositionLocalProvider(
        LocalFengShiColors provides fengShiColors
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}

