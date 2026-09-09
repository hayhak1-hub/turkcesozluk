package com.hayhak.turkcesozluk.data.db

import java.text.Collator
import java.util.Locale

enum class AppLocale(val tag: String, val nativeName: String) {
    SYSTEM("", "System"),
    TURKISH("tr", "Türkiye Türkçesi"),
    AZERBAIJANI("az", "Azərbaycan"),
    KAZAKH("kk", "Қазақша"),
    KYRGYZ("ky", "Кыргызча"),
    UZBEK("uz", "Oʻzbek"),
    TURKMEN("tk", "Türkmen"),
    ENGLISH("en", "English"),
    CHINESE("zh-CN", "中文"),
    HINDI("hi", "हिन्दी"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    ARABIC("ar", "العربية"),
    BENGALI("bn", "বাংলা"),
    PORTUGUESE("pt", "Português"),
    RUSSIAN("ru", "Русский"),
    URDU("ur", "اردو"),
    INDONESIAN("id", "Bahasa Indonesia"),
    GERMAN("de", "Deutsch"),
    JAPANESE("ja", "日本語"),
    SWAHILI("sw", "Kiswahili"),
    MARATHI("mr", "मराठी"),
    TELUGU("te", "తెలుగు"),
    TAMIL("ta", "தமிழ்"),
    VIETNAMESE("vi", "Tiếng Việt"),
    KOREAN("ko", "한국어"),
    ;

    companion object {
        private val nameCollator = Collator.getInstance(Locale.ROOT).apply {
            strength = Collator.PRIMARY
        }

        /** System first, then all languages alphabetically by native name. */
        val sortedForPicker: List<AppLocale> = buildList {
            add(SYSTEM)
            addAll(
                entries.filter { it != SYSTEM }
                    .sortedWith(compareBy(nameCollator) { it.nativeName })
            )
        }

        fun fromTag(tag: String?): AppLocale =
            entries.firstOrNull { it.tag == tag } ?: SYSTEM
    }
}
