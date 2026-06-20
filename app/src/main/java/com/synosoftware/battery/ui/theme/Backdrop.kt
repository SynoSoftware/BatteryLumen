package com.synosoftware.battery.ui.theme

import androidx.compose.ui.graphics.Color

const val ChromeAlpha = 0.92f
const val NavigationAlpha = 0.96f

fun appBackdropColors(darkTheme: Boolean): List<Color> {
    return if (darkTheme) {
        listOf(DarkBackgroundTop, DarkBackgroundMid, DarkBackgroundBottom)
    } else {
        listOf(LightBackgroundTop, LightBackgroundMid, LightBackgroundBottom)
    }
}

fun appChromeColor(darkTheme: Boolean): Color {
    return if (darkTheme) DarkChrome else LightChrome
}
