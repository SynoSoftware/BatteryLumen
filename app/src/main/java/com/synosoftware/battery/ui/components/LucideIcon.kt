package com.synosoftware.battery.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun LucideIcon(
    @DrawableRes resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier,
        painter = painterResource(resId),
        contentDescription = contentDescription,
    )
}
