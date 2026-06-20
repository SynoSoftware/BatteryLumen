package com.synosoftware.battery.i18n

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.fail
import org.junit.Test

class LocalizationCatalogTest {
    @Test
    fun englishCatalogDoesNotContainPlaceholderCopy() {
        val strings = loadStrings()

        val placeholderPattern = Regex("""\b(?:[A-Z][a-z]+ ){1,4}(?:Page Title|Subtitle|Desc|Body|Title)\b""")
        val violations = strings.filter { entry ->
            val value = entry.value
            value.contains("Battery tab", ignoreCase = true) ||
                value.contains("Page Title", ignoreCase = true) ||
                value.contains("Desc", ignoreCase = true) ||
                value.contains("Â") ||
                placeholderPattern.containsMatchIn(value)
        }

        if (violations.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("Found placeholder-looking localization strings:")
                    violations.forEach { entry ->
                        appendLine("${entry.path.joinToString(".")} = ${entry.value}")
                    }
                },
            )
        }
    }

    @Test
    fun englishCatalogDoesNotUseLegacyGeneratedFamilies() {
        val strings = loadStrings()
        val paths = strings.map { it.path.joinToString(".") }

        val underscoredPaths = paths.filter { path ->
            path.split('.').any { segment -> segment.contains('_') }
        }
        val legacyPrefixes = listOf("battery.tab", "how.it.works", "ledger", "history")
        val legacyPaths = paths.filter { path ->
            legacyPrefixes.any { legacy -> path == legacy || path.startsWith("$legacy.") }
        }

        if (underscoredPaths.isNotEmpty() || legacyPaths.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("Found legacy localization paths:")
                    if (underscoredPaths.isNotEmpty()) {
                        appendLine("Underscored paths:")
                        underscoredPaths.sorted().forEach { appendLine(it) }
                    }
                    if (legacyPaths.isNotEmpty()) {
                        appendLine("Legacy family paths:")
                        legacyPaths.sorted().forEach { appendLine(it) }
                    }
                },
            )
        }
    }

    @Test
    fun englishCatalogContainsEveryLiteralTKeyInSource() {
        val strings = loadStrings()
        val catalogPaths = strings.map { it.path.joinToString(".") }.toSet()
        val sourceKeys = collectLiteralKeys()
        val missing = sourceKeys.filterNot(catalogPaths::contains).sorted()

        if (missing.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("Missing localization keys referenced in source:")
                    missing.forEach { appendLine(it) }
                },
            )
        }
    }

    private fun loadStrings(): List<CatalogString> {
        val catalogFile = listOf(
            File("src/main/assets/i18n/en.json"),
            File("app/src/main/assets/i18n/en.json"),
        ).firstOrNull { it.exists() }
            ?: fail("Could not find src/main/assets/i18n/en.json")

        val root = Json.parseToJsonElement(Files.readString(Path.of(catalogFile.toString())).removePrefix("\uFEFF"))
        val strings = mutableListOf<CatalogString>()
        collectStrings(root, emptyList(), strings)
        return strings
    }

    private fun collectLiteralKeys(): Set<String> {
        val roots = listOf(
            File("app/src/main/java"),
            File("app/src/androidTest/java"),
            File("app/src/test/java"),
        ).filter { it.exists() }

        val pattern = Regex("""\bT\("([^"]+)""")
        val keys = mutableSetOf<String>()
        roots.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    pattern.findAll(file.readText()).forEach { match ->
                        keys += match.groupValues[1]
                    }
                }
        }
        return keys
    }

    private fun collectStrings(
        element: JsonElement,
        path: List<String>,
        output: MutableList<CatalogString>,
    ) {
        when (element) {
            is JsonObject -> element.forEach { (key, value) ->
                collectStrings(value, path + key, output)
            }
            is JsonArray -> element.forEachIndexed { index, value ->
                collectStrings(value, path + index.toString(), output)
            }
            is JsonPrimitive -> if (element.isString) {
                output += CatalogString(path, element.content)
            }
        }
    }

    private data class CatalogString(
        val path: List<String>,
        val value: String,
    )
}
