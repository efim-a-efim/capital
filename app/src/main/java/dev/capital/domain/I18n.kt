package dev.capital.domain

import java.util.Locale

// English text is the key, as in gettext: layers without an Android context translate too, and an empty table means English.
object I18n {
    val languages = linkedMapOf(
        "en" to "English", "zh" to "中文", "hi" to "हिन्दी", "es" to "Español", "ar" to "العربية",
        "fr" to "Français", "bn" to "বাংলা", "pt" to "Português", "ru" to "Русский", "id" to "Bahasa Indonesia",
        "ur" to "اردو", "de" to "Deutsch", "ja" to "日本語", "mr" to "मराठी", "vi" to "Tiếng Việt",
    )
    const val SYSTEM = "system"
    @Volatile var table: Map<String, String> = emptyMap()
    @Volatile var locale: Locale = Locale.getDefault()
    val rtl get() = locale.language in listOf("ar", "ur")
    // Stored choice, else the device language, else English. Android reports Indonesian as the legacy code "in".
    fun resolve(choice: String?, device: Locale): String =
        choice?.takeIf { it in languages } ?: device.language.let { if (it == "in") "id" else it }.takeIf { it in languages } ?: "en"
    // The device locale keeps its region for number and date formats when the language already matches.
    fun use(code: String, device: Locale, translations: Map<String, String>) {
        table = translations
        locale = if (resolve(null, device) == code) device else Locale.forLanguageTag(code)
    }
    // English lives at the site root, every other language under its code.
    fun siteUrl(path: String): String = resolve(null, locale).let { "https://capital.fimych.dev/" + (if (it == "en") "" else "$it/") + path }
    fun helpUrl(screen: String): String = siteUrl("screens/$screen")
}

private val placeholder = Regex("\\{(\\d+)\\}")
// ponytail: no plural forms; counts are phrased so that one template fits every number
fun tr(template: String, vararg args: Any?): String =
    (I18n.table[template] ?: template).let { text -> if (args.isEmpty()) text else placeholder.replace(text) { m -> args.getOrNull(m.groupValues[1].toInt())?.toString() ?: m.value } }
