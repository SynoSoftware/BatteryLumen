package com.synosoftware.battery.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal600,
    onPrimary = Color.White,
    primaryContainer = Teal300,
    onPrimaryContainer = Teal600,
    secondary = Amber600,
    onSecondary = Color.White,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = Amber600,
    tertiary = Green300,
    onTertiary = Color.Black,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = Slate100,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Slate600,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = Slate500,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    errorContainer = LightErrorContainer,
    onError = Color.White,
    onErrorContainer = LightOnErrorContainer,
    surfaceTint = Teal600,
)

private val DarkColors = darkColorScheme(
    primary = Teal300,
    onPrimary = Slate950,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = Slate300,
    onSecondary = Slate950,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = Green300,
    onTertiary = Slate950,
    tertiaryContainer = Amber300,
    onTertiaryContainer = Amber600,
    background = DarkBackgroundBottom,
    onBackground = Slate200,
    surface = DarkBackgroundMid,
    onSurface = Slate200,
    surfaceVariant = Slate600,
    onSurfaceVariant = Slate300,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = Slate500,
    outlineVariant = DarkOutlineVariant,
    error = Red300,
    errorContainer = DarkErrorContainer,
    onError = Color.White,
    onErrorContainer = DarkOnErrorContainer,
    surfaceTint = Teal300,
)

@Composable
fun BatteryTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        typography = AppTypography,
        content = content,
    )
}
