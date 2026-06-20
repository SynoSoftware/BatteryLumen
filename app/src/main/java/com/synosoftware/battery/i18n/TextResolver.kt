package com.synosoftware.battery.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

private const val LocalizationAsset = "i18n/en.json"

private val LocalizationJson = Json {
    ignoreUnknownKeys = true
}

private val LocalizationLock = Any()

@Volatile
private var localizationCatalog: JsonObject? = null

fun Context.resolveText(text: UiText): String {
    val resolved = resolveCatalogValue(text.key)
    return if (resolved != null) {
        formatText(resolved, text.args, this)
    } else {
        fallbackText(text)
    }
}

@Composable
fun UiText.asString(): String {
    return LocalContext.current.resolveText(this)
}

private fun Context.resolveCatalogValue(key: String): String? {
    val catalog = localizationCatalog ?: synchronized(LocalizationLock) {
        localizationCatalog ?: runCatching {
            assets.open(LocalizationAsset).use { stream ->
                LocalizationJson.parseToJsonElement(
                    stream.bufferedReader().readText().removePrefix("\uFEFF"),
                ).jsonObject
            }
        }.getOrNull()?.also { localizationCatalog = it }
    } ?: return null

    return catalog.resolveKey(key)
}

private fun JsonObject.resolveKey(key: String): String? {
    val direct = this[key]
    if (direct is JsonPrimitive && direct.isString) {
        return direct.content
    }

    val dotPath = resolvePath(key, '.')
    if (dotPath != null) {
        return dotPath
    }

    var current: JsonElement = this
    for (segment in key.split('_')) {
        current = when (current) {
            is JsonObject -> current[segment] ?: return null
            else -> return null
        }
    }

    return when (current) {
        is JsonPrimitive -> current.content
        is JsonObject -> {
            val leaf = current["_"]
            if (leaf is JsonPrimitive && leaf.isString) leaf.content else null
        }
        else -> null
    }
}

private fun JsonObject.resolvePath(key: String, delimiter: Char): String? {
    if (delimiter !in key) return null

    var current: JsonElement = this
    for (segment in key.split(delimiter)) {
        current = when (current) {
            is JsonObject -> current[segment] ?: return null
            else -> return null
        }
    }

    return when (current) {
        is JsonPrimitive -> current.content
        is JsonObject -> {
            val leaf = current["_"]
            if (leaf is JsonPrimitive && leaf.isString) leaf.content else null
        }
        else -> null
    }
}

private fun formatText(template: String, args: List<UiArg>, context: Context): String {
    if (args.isEmpty()) return template
    return runCatching {
        val resolvedArgs = args.map { it.resolve(context) }.toTypedArray()
        String.format(Locale.ROOT, template, *resolvedArgs)
    }.getOrElse {
        buildString {
            append(template)
            append(": ")
            append(args.joinToString(", ") { it.resolve(context) })
        }
    }
}

private fun Context.fallbackText(text: UiText): String {
    val label = text.key
        .replace('.', ' ')
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
            append(text.args.joinToString(", ") { it.resolve(this@fallbackText) })
        }
    }
}

private fun UiArg.resolve(context: Context): String {
    return when (this) {
        is TextArg -> value
        is TextRef -> context.resolveText(value)
    }
}
