package com.synosoftware.battery.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

fun Context.resolveText(text: UiText): String {
    val resId = resources.getIdentifier(text.key, "string", packageName)
    check(resId != 0) { "Missing string resource for key: ${text.key}" }
    return if (text.args.isEmpty()) {
        getString(resId)
    } else {
        getString(resId, *text.args.toTypedArray())
    }
}

@Composable
fun UiText.asString(): String {
    return LocalContext.current.resolveText(this)
}
