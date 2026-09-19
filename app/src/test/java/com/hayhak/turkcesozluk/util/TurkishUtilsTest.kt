package com.hayhak.turkcesozluk.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TurkishUtilsTest {

    @Test
    fun `normalize trims and lowercases using Turkish rules`() {
        assertEquals("istasyon", "  İSTASYON  ".normalizeTR())
        assertEquals("ışık", "IŞIK".normalizeTR())
        assertEquals("ilgi", "İlgi".normalizeTR())
    }

    @Test
    fun `normalize is not affected by the default locale`() {
        val previous = Locale.getDefault()
        try {
            // Varsayılan yerel ayar Türkçe değilken de aynı sonucu vermeli.
            Locale.setDefault(Locale("en", "US"))
            assertEquals("istasyon", "İSTASYON".normalizeTR())
            assertEquals("ışık", "IŞIK".normalizeTR())
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `capitalize maps dotted and dotless i correctly`() {
        assertEquals("İstasyon", "istasyon".capitalizeTR())
        assertEquals("Işık", "ışık".capitalizeTR())
        assertEquals("Elma", "elma".capitalizeTR())
    }

    @Test
    fun `capitalize leaves the rest of the word untouched`() {
        assertEquals("Kitap okumak", "kitap okumak".capitalizeTR())
        assertEquals("", "".capitalizeTR())
    }

    @Test
    fun `normalize then capitalize round trips a shouty word`() {
        assertEquals("İstanbul", "İSTANBUL".normalizeTR().capitalizeTR())
    }
}
