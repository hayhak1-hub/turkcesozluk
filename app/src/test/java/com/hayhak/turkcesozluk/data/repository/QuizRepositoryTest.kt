package com.hayhak.turkcesozluk.data.repository

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuizRepositoryTest {

    private val repository = QuizRepository()

    @Before
    fun setUp() = SynonymDataStore.clear()

    @After
    fun tearDown() {
        SynonymDataStore.clear()
        SynonymDataStore.updateCache(emptySet())
    }

    private fun seed(entryCount: Int) {
        val data = (1..entryCount).associate { "kelime$it" to setOf("anlam$it") }
        SynonymDataStore.putAll(data)
        SynonymDataStore.updateCache(data.keys)
    }

    @Test
    fun `returns null while the dictionary is still empty`() {
        SynonymDataStore.updateCache(emptySet())
        assertNull(repository.getRandomWord())
        assertNull(repository.generateQuestion())
    }

    @Test
    fun `question words come from primary keys, never from reverse mappings`() {
        val primary = setOf("güzel")
        SynonymDataStore.putAll(
            mapOf(
                "güzel" to setOf("hoş"),
                // Ters eşleme: uzun bir açıklama cümlesi de haritada anahtardır ama
                // soru kökü olarak kullanılmamalıdır.
                "çok hoş ve sevimli bir görünüm" to setOf("güzel")
            )
        )
        SynonymDataStore.updateCache(primary)

        repeat(30) {
            val word = repository.getRandomWord()?.first
            assertEquals("güzel", word)
        }
    }

    @Test
    fun `generated question always has four distinct options including the answer`() {
        seed(50)
        repeat(50) {
            val question = repository.generateQuestion()
            assertNotNull(question)
            question!!
            assertEquals(4, question.options.size)
            assertEquals(4, question.options.toSet().size)
            assertTrue(question.correctAnswer in question.options)
            assertTrue(question.options.none { it.isBlank() })
        }
    }

    @Test
    fun `no question is generated when there are too few distractors`() {
        // Tek kelime var; 3 çeldirici üretilemez, dolayısıyla soru atlanmalı.
        seed(1)
        assertNull(repository.generateQuestion())
    }

    @Test
    fun `distractors exclude the given words`() {
        seed(40)
        val excluded = setOf("anlam1", "anlam2")
        val distractors = repository.getRandomDistractors(3, excluding = excluded)
        assertEquals(3, distractors.size)
        assertTrue(distractors.none { it in excluded })
        assertEquals(distractors.size, distractors.toSet().size)
    }

    @Test
    fun `words without a usable answer are skipped`() {
        SynonymDataStore.putAll(mapOf("boşluk" to setOf("  ")))
        SynonymDataStore.updateCache(setOf("boşluk"))
        assertNull(repository.getRandomWord())
    }
}
