package com.synosoftware.battery.i18n

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@Composable
fun T(@StringRes resId: Int, vararg args: Any): String = stringResource(resId, *args)

fun Context.T(@StringRes resId: Int, vararg args: Any): String = getString(resId, *args)

@Composable
fun UiText.asString(): String = LocalContext.current.resolveText(this)

fun Context.resolveText(text: UiText): String = when (text) {
    is UiText.Res -> getString(text.resId, *text.args.resolve(this))
}

private fun List<UiTextArg>.resolve(context: Context): Array<Any> = map { it.resolve(context) }.toTypedArray()

private fun UiTextArg.resolve(context: Context): Any = when (this) {
    is StringArg -> value
    is TextArg -> context.resolveText(value)
}
