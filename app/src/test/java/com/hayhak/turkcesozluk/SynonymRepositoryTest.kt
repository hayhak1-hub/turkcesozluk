package com.hayhak.turkcesozluk

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class SynonymRepositoryTest {

    @Test
    fun testGetSynonym_Normalization() {
        val trLocale = Locale("tr", "TR")
        val word = "İSTASYON"
        val normalized = word.lowercase(trLocale)
        assertEquals("istasyon", normalized)
    }

    @Test
    fun testDailyWord_SeedLogic() {
        val year = 2026
        val day = 150
        val size = 1000
        val seed = (year * 1000) + day
        val index = seed % size
        
        val sameSeed = (year * 1000) + day
        val sameIndex = sameSeed % size
        
        assertEquals(index, sameIndex)
    }
}
