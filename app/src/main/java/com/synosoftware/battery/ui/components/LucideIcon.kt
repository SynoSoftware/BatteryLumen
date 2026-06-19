package com.synosoftware.battery.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun LucideIcon(
    @DrawableRes resId: Int,
    contentDescription: String?,
) {
    Icon(
        painter = painterResource(resId),
        contentDescription = contentDescription,
    )
}
