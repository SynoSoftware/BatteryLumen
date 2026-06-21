package com.synosoftware.battery.i18n

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

@Serializable
sealed interface UiText {
    @Serializable
    data class Res(
        @StringRes val resId: Int,
        val args: List<UiTextArg> = emptyList(),
    ) : UiText
}

@Serializable
sealed interface UiTextArg

@Serializable
data class StringArg(
    val value: String,
) : UiTextArg

@Serializable
data class TextArg(
    val value: UiText,
) : UiTextArg

fun TR(
    @StringRes resId: Int,
    vararg args: Any?,
): UiText {
    return UiText.Res(
        resId = resId,
        args = args.map { value ->
            when (value) {
                null -> StringArg("")
                is UiText -> TextArg(value)
                else -> StringArg(value.toString())
            }
        },
    )
}
