package com.synosoftware.battery.i18n

import kotlinx.serialization.Serializable

@Serializable
data class UiText(
    val key: String,
    val args: List<UiArg> = emptyList(),
)

@Serializable
sealed interface UiArg

@Serializable
data class TextArg(
    val value: String,
) : UiArg

@Serializable
data class TextRef(
    val value: UiText,
) : UiArg

fun T(
    key: String,
    vararg args: Any?,
): UiText {
    return UiText(
        key = key,
        args = args.map { value ->
            when (value) {
                null -> TextArg("")
                is UiText -> TextRef(value)
                else -> TextArg(value.toString())
            }
        },
    )
}
