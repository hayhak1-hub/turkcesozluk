package com.hayhak.turkcesozluk.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvLoaderParseTest {

    private fun parse(line: String) = CsvLoader.parseCsvLine(line)

    @Test
    fun `splits a plain row on commas`() {
        assertEquals(
            listOf("abajurlu", "Abajuru olan", "", ""),
            parse("abajurlu,Abajuru olan,,")
        )
    }

    @Test
    fun `commas inside quotes do not split the field`() {
        assertEquals(
            listOf(
                "abandone etmek",
                "dövüşemeyecek duruma getirmek, sersemletmek [spor] | çaresiz duruma düşürmek",
                ""
            ),
            parse("abandone etmek,\"dövüşemeyecek duruma getirmek, sersemletmek [spor] | çaresiz duruma düşürmek\",")
        )
    }

    @Test
    fun `doubled quotes inside a quoted field become a single quote`() {
        assertEquals(
            listOf("madde", "so called \"anlam\" burada"),
            parse("madde,\"so called \"\"anlam\"\" burada\"")
        )
    }

    @Test
    fun `a quoted field containing only a doubled quote is preserved`() {
        assertEquals(listOf("a", "\"", "b"), parse("a,\"\"\"\",b"))
    }

    @Test
    fun `trailing empty fields are kept`() {
        assertEquals(listOf("aba", "kumaş", "", ""), parse("aba,kumaş,,"))
    }

    @Test
    fun `an empty line yields a single empty field`() {
        assertEquals(listOf(""), parse(""))
    }

    @Test
    fun `unterminated quote swallows the rest of the line`() {
        // Bozuk satırda da kilitlenmeden bir sonuç dönmeli.
        assertEquals(listOf("a", "b,c"), parse("a,\"b,c"))
    }
}
