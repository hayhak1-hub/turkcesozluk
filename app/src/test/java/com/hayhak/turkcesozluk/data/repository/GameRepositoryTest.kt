package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.game.WordleEngine
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameRepositoryTest {

    private val repository = GameRepository(QuizRepository())

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
    fun `returns null when the dictionary is empty`() {
        SynonymDataStore.updateCache(emptySet())
        assertNull(repository.newTarget())
    }

    @Test
    fun `only single words of playable length are offered`() {
        seed(
            mapOf(
                "ev" to setOf("konut"),                       // çok kısa
                "kütüphaneci" to setOf("kitapçı"),            // çok uzun
                "abandone etmek" to setOf("sersemletmek"),    // boşluklu deyim
                "kitap" to setOf("yapıt"),                    // uygun
            ),
            primary = setOf("ev", "kütüphaneci", "abandone etmek", "kitap")
        )

        assertEquals(listOf("kitap"), repository.playableWords())
        repeat(20) {
            assertEquals("kitap", repository.newTarget()?.word)
        }
    }

    @Test
    fun `no target when the mode has no playable word`() {
        // Deyimler kipindeki gibi hepsi çok kelimeli
        seed(
            mapOf("abandone etmek" to setOf("sersemletmek"), "göz atmak" to setOf("bakmak")),
            primary = setOf("abandone etmek", "göz atmak")
        )
        assertTrue(repository.playableWords().isEmpty())
        assertNull(repository.newTarget())
    }

    @Test
    fun `a clue that contains the answer is rejected`() {
        assertFalse(repository.isUsableClue("çok güzel", "güzel"))
        assertFalse(repository.isUsableClue("   ", "güzel"))
        assertTrue(repository.isUsableClue("hoş", "güzel"))
    }

    @Test
    fun `words whose only clue gives away the answer are skipped`() {
        seed(
            mapOf(
                "güzel" to setOf("çok güzel"),   // ipucu cevabı içeriyor -> atlanmalı
                "kitap" to setOf("yapıt"),
            ),
            primary = setOf("güzel", "kitap")
        )
        repeat(20) {
            assertEquals("kitap", repository.newTarget()?.word)
        }
    }

    @Test
    fun `the produced target is consistent with the engine rules`() {
        seed(mapOf("kitap" to setOf("yapıt"), "deniz" to setOf("derya")))
        val target = repository.newTarget()
        assertNotNull(target)
        target!!
        assertTrue(WordleEngine.isPlayableWord(target.word, GAME_MIN_LENGTH, GAME_MAX_LENGTH))
        assertTrue(target.clue.isNotBlank())
        assertFalse(target.clue.contains(target.word))
    }

    @Test
    fun `the playable list is rebuilt when the dictionary mode changes`() {
        seed(mapOf("kitap" to setOf("yapıt")))
        assertEquals(listOf("kitap"), repository.playableWords())

        // Kip değişimi: SynonymDataStore yeni bir primaryKeys listesi yayımlar.
        SynonymDataStore.clear()
        seed(mapOf("deniz" to setOf("derya")))
        assertEquals(listOf("deniz"), repository.playableWords())
    }

    @Test
    fun `quiz options are empty when there are not enough distractors`() {
        // Tek karşılık var; 3 çeldirici üretilemez.
        seed(mapOf("kitap" to setOf("yapıt")))
        assertEquals(emptyList<String>(), repository.newTarget()?.quizOptions)
    }

    @Test
    fun `quiz options hold four distinct choices including the clue`() {
        val data = (1..40).associate { "kelime$it" to setOf("anlam$it") } + mapOf("kitap" to setOf("yapıt"))
        seed(data, primary = setOf("kitap"))

        val target = repository.newTarget()
        assertNotNull(target)
        val options = target!!.quizOptions
        assertEquals(4, options.size)
        assertEquals(4, options.toSet().size)
        assertTrue(options.any { it.equals("yapıt", ignoreCase = true) })
    }
}
