package com.hayhak.turkcesozluk.util

import java.util.Locale

object TurkishUtils {
    val trLocale = Locale("tr", "TR")

    /**
     * Verilen metni Türkçe kurallarına göre normalize eder (trim + lowercase).
     */
    fun normalize(text: String): String {
        return text.trim().lowercase(trLocale)
    }

    /**
     * Verilen metnin ilk harfini Türkçe kurallarına göre büyütür.
     * 'i' -> 'İ'
     * 'ı' -> 'I'
     */
    fun capitalize(text: String): String {
        if (text.isEmpty()) return text
        
        val firstChar = text[0]
        val rest = text.substring(1)
        
        val newFirst = when (firstChar) {
            'i' -> "İ"
            'ı' -> "I"
            else -> firstChar.toString().uppercase(trLocale)
        }
        
        return newFirst + rest
    }
}

/**
 * String için kolay kullanım sağlayan extension fonksiyonlar
 */
fun String.normalizeTR(): String = TurkishUtils.normalize(this)
fun String.capitalizeTR(): String = TurkishUtils.capitalize(this)
