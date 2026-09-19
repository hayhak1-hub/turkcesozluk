package com.hayhak.turkcesozluk.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DictionaryRepositoryTest {

    private val repository = DictionaryRepository()

    @Before
    fun setUp() = SynonymDataStore.clear()

    @After
    fun tearDown() {
        SynonymDataStore.clear()
        SynonymDataStore.updateCache(emptySet())
    }

    private fun seed(data: Map<String, Set<String>>, primary: Set<String> = data.keys) {
        SynonymDataStore.putAll(data)
        SynonymDataStore.updateCache(primary)
    }

    @Test
    fun `lookups normalize the query`() {
        seed(mapOf("istasyon" to setOf("gar", "durak")))
        assertEquals(listOf("gar", "durak"), repository.getSynonyms(" İSTASYON "))
        assertEquals("gar, durak", repository.getSynonym("İstasyon"))
    }

    @Test
    fun `suggestions are prefix matches and respect the limit`() {
        seed(mapOf("kitap" to setOf("a"), "kitaplık" to setOf("b"), "kitapçı" to setOf("c"), "masa" to setOf("d")))
        val suggestions = repository.getSuggestions("kitap", limit = 2)
        assertEquals(2, suggestions.size)
        assertTrue(suggestions.all { it.startsWith("kitap") })
        assertTrue(repository.getSuggestions("   ").isEmpty())
    }

    @Test
    fun `daily word is null until the data is ready`() {
        SynonymDataStore.updateCache(emptySet())
        assertNull(repository.getDailyWord())
        assertNull(repository.getRandomWord())
    }

    @Test
    fun `daily word is stable within the same day and capitalized`() {
        seed(mapOf("istasyon" to setOf("gar"), "güzel" to setOf("hoş"), "ışık" to setOf("nur")))
        val first = repository.getDailyWord()
        val second = repository.getDailyWord()
        assertEquals(first, second)
        val word = first!!.first
        assertTrue(word in listOf("İstasyon", "Güzel", "Işık"))
    }

    @Test
    fun `word tree only branches on short synonyms`() {
        seed(
            mapOf(
                "güzel" to setOf("hoş", "gözü gönlü açan çok hoş bir görünüm"),
                "hoş" to setOf("şirin", "güzel"),
                "gözü gönlü açan çok hoş bir görünüm" to setOf("güzel")
            )
        )
        val tree = repository.getWordTree("güzel")
        assertTrue("hoş" in tree)
        assertTrue("gözü gönlü açan çok hoş bir görünüm" !in tree)
        // Kök kelimenin kendisi alt dalda tekrar etmemeli.
        assertTrue(tree.getValue("hoş").none { it == "güzel" })
    }

    @Test
    fun `manually added words become searchable in both directions`() = runBlocking {
        seed(mapOf("istasyon" to setOf("gar")))
        repository.addWordManual(" ÖRNEK ", " Numune ")

        assertEquals(listOf("numune"), repository.getSynonyms("örnek"))
        assertEquals(listOf("örnek"), repository.getSynonyms("numune"))
        assertTrue("örnek" in SynonymDataStore.primaryKeys)
        assertTrue("örnek" in SynonymDataStore.cachedKeys)
    }

    @Test
    fun `blank input is ignored when adding a word`() = runBlocking {
        seed(mapOf("istasyon" to setOf("gar")))
        repository.addWordManual("  ", "numune")
        assertEquals(1, repository.synonymsCount)
    }
}
