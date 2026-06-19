package com.synosoftware.battery.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

fun Context.resolveText(text: UiText): String {
    val resId = resources.getIdentifier(text.key, "string", packageName)
    return if (resId != 0) {
        if (text.args.isEmpty()) {
            getString(resId)
        } else {
            getString(resId, *text.args.toTypedArray())
        }
    } else {
        fallbackText(text)
    }
}

@Composable
fun UiText.asString(): String {
    return LocalContext.current.resolveText(this)
}

private fun Context.fallbackText(text: UiText): String {
    val label = text.key
        .replace('_', ' ')
        .replace(Regex("\\b(v\\d+)\\b"), "")
        .trim()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    return if (text.args.isEmpty()) {
        label
    } else {
        buildString {
            append(label)
            append(": ")
            append(text.args.joinToString(", "))
        }
    }
}
