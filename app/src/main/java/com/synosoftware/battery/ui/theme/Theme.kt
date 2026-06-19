package com.synosoftware.battery.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal500,
    onPrimary = Color.Black,
    secondary = Amber500,
    onSecondary = Color.Black,
    tertiary = Green300,
    background = Color(0xFFF6F7FA),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFE7EBF0),
    onSurfaceVariant = Slate800,
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Teal300,
    onPrimary = Slate950,
    secondary = Amber300,
    onSecondary = Slate950,
    tertiary = Green300,
    background = Slate950,
    onBackground = Slate200,
    surface = Slate900,
    onSurface = Slate200,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate200,
    error = Red300,
)

@Composable
fun BatteryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
