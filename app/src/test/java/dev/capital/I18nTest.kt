package dev.capital

import dev.capital.domain.I18n
import dev.capital.domain.tr
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class I18nTest {
    private val call = Regex("""\btr\("((?:[^"\\]|\\.)*)"""")
    private val slot = Regex("\\{\\d+\\}")
    private fun unescape(s: String) = s.replace("\\n", "\n").replace("\\\"", "\"").replace("\\$", "$").replace("\\\\", "\\")
    private fun templates() = File("src/main/java").walkTopDown().filter { it.extension == "kt" }.flatMap { f -> call.findAll(f.readText()).map { unescape(it.groupValues[1]) } }.toSet()
    @After fun reset() { I18n.use("en", Locale.getDefault(), emptyMap()) }

    @Test fun everyLanguageCoversEveryTemplate() {
        val used = templates()
        assertTrue("templates found", used.size > 100)
        assertEquals(15, I18n.languages.size)
        (I18n.languages.keys - "en").forEach { code ->
            val table = Json.decodeFromString<Map<String, String>>(File("src/main/assets/i18n/$code.json").readText())
            assertEquals("$code missing", emptySet<String>(), used - table.keys)
            assertEquals("$code unused", emptySet<String>(), table.keys - used)
            table.forEach { (k, v) ->
                assertTrue("$code blank: $k", v.isNotBlank())
                assertEquals("$code placeholders: $k", slot.findAll(k).map { it.value }.toSet(), slot.findAll(v).map { it.value }.toSet())
            }
        }
    }
    @Test fun trNeverTakesAVariable() {
        val bad = File("src/main/java").walkTopDown().filter { it.extension == "kt" && it.name != "I18n.kt" }.flatMap { f -> Regex("""\btr\((?!")""").findAll(f.readText()).map { f.name } }.toList()
        assertEquals(emptyList<String>(), bad)
    }
    @Test fun translatesAndFillsPlaceholders() {
        assertEquals("Delete A?", tr("Delete {0}?", "A"))
        I18n.use("ru", Locale.US, mapOf("Delete {0}?" to "Удалить {0}?", "{0} of {1}" to "{1} из {0}"))
        assertEquals("Удалить A?", tr("Delete {0}?", "A"))
        assertEquals("2 из 1", tr("{0} of {1}", 1, 2))
        assertEquals("Unknown {0}", tr("Unknown {0}"))
    }
    @Test fun languageFollowsChoiceThenDeviceThenEnglish() {
        assertEquals("ru", I18n.resolve("ru", Locale.US))
        assertEquals("de", I18n.resolve(I18n.SYSTEM, Locale.GERMANY))
        assertEquals("id", I18n.resolve(null, Locale.forLanguageTag("in-ID")))
        assertEquals("en", I18n.resolve(null, Locale.forLanguageTag("sv-SE")))
        I18n.use("ar", Locale.US, emptyMap()); assertTrue(I18n.rtl)
    }
    @Test fun helpUrlFollowsLanguage() {
        I18n.use("en", Locale.US, emptyMap()); assertEquals("https://capital.fimych.dev/screens/goals", I18n.helpUrl("goals"))
        I18n.use("ru", Locale.US, emptyMap()); assertEquals("https://capital.fimych.dev/ru/screens/bucket", I18n.helpUrl("bucket"))
        I18n.use("id", Locale.US, emptyMap()); assertEquals("https://capital.fimych.dev/id/screens/start", I18n.helpUrl("start"))
    }
}
