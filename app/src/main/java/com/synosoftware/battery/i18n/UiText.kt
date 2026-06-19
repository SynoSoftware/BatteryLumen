package com.synosoftware.battery.i18n

data class UiText(
    val key: String,
    val args: List<String> = emptyList(),
)

fun text(
    key: String,
    vararg args: Any?,
): UiText {
    return UiText(
        key = key,
        args = args.map { value -> value?.toString().orEmpty() },
    )
}
