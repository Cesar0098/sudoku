package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.data.db.AppThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF175090),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE9F7),
    onPrimaryContainer = Color(0xFF0D3562),
    secondary = Color(0xFF526070),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E4F2),
    onSecondaryContainer = Color(0xFF131D28),
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF282828),
    surface = Color(0xFFF7F7F7),
    onSurface = Color(0xFF282828),
    surfaceVariant = Color(0xFFE8EEF6),
    onSurfaceVariant = Color(0xFF5A667A),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7E99C8),
    onPrimary = Color(0xFF0F2B48),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD6E8FC),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF222F3E),
    onSecondaryContainer = Color(0xFFE2E8F0),
    background = Color(0xFF000000),
    onBackground = Color(0xFFD6DCE3),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFD6DCE3),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF7D4E3A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8D5C4),
    onPrimaryContainer = Color(0xFF3E2319),
    background = Color(0xFFF5EFEB),
    onBackground = Color(0xFF3E2F23),
    surface = Color(0xFFFAF7F2),
    onSurface = Color(0xFF3E2F23),
    surfaceVariant = Color(0xFFEAE2D8),
    onSurfaceVariant = Color(0xFF6B5848),
    outline = Color(0xFFD3C5B4)
)

@Composable
fun SudokuTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT, AppThemeMode.SEPIA -> false
    }

    val (colorScheme, sudokuColors) = when (themeMode) {
        AppThemeMode.SEPIA -> Pair(SepiaColorScheme, SepiaSudokuColors)
        AppThemeMode.DARK -> Pair(DarkColorScheme, DarkSudokuColors)
        AppThemeMode.LIGHT -> Pair(LightColorScheme, LightSudokuColors)
        AppThemeMode.SYSTEM -> if (isDark) Pair(DarkColorScheme, DarkSudokuColors) else Pair(LightColorScheme, LightSudokuColors)
    }

    CompositionLocalProvider(LocalSudokuColors provides sudokuColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
