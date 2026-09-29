package dev.capital

import android.content.Context
import dev.capital.domain.I18n
import kotlinx.serialization.json.Json
import java.util.Locale

// The language is a device preference, not portfolio data: the lock screen needs it before anything is decrypted.
object Language {
    private fun prefs(context: Context) = context.getSharedPreferences("capital-ui", Context.MODE_PRIVATE)
    fun choice(context: Context): String = prefs(context).getString("language", null)?.takeIf { it in I18n.languages } ?: I18n.SYSTEM
    fun choose(context: Context, choice: String) { prefs(context).edit().putString("language", choice).apply(); apply(context) }
    fun apply(context: Context) {
        val code = I18n.resolve(choice(context), Locale.getDefault())
        // A missing or broken file falls back to English instead of stopping the app.
        val table = if (code == "en") emptyMap() else runCatching { context.assets.open("i18n/$code.json").use { Json.decodeFromString<Map<String, String>>(it.readBytes().toString(Charsets.UTF_8)) } }.getOrDefault(emptyMap())
        I18n.use(code, Locale.getDefault(), table)
    }
}
