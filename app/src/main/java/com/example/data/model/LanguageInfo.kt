package com.example.data.model

data class LanguageInfo(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val isRtl: Boolean = false,
    val flag: String = ""
)

object Languages {
    val ALL = listOf(
        LanguageInfo(code = "en", nativeName = "English", englishName = "English", isRtl = false, flag = "🇬🇧"),
        LanguageInfo(code = "ar", nativeName = "العربية", englishName = "Arabic", isRtl = true, flag = "🇴🇲"),
        LanguageInfo(code = "bn", nativeName = "বাংলা", englishName = "Bengali", isRtl = false, flag = "🇧🇩"),
        LanguageInfo(code = "ur", nativeName = "اردو", englishName = "Urdu", isRtl = true, flag = "🇵🇰"),
        LanguageInfo(code = "hi", nativeName = "हिन्दी", englishName = "Hindi", isRtl = false, flag = "🇮🇳"),
        LanguageInfo(code = "es", nativeName = "Español", englishName = "Spanish", isRtl = false, flag = "🇪🇸"),
        LanguageInfo(code = "fr", nativeName = "Français", englishName = "French", isRtl = false, flag = "🇫🇷")
    )

    fun find(code: String): LanguageInfo {
        return ALL.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ALL.first()
    }
}
