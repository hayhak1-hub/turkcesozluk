package com.hayhak.turkcesozluk.viewmodel

import com.hayhak.turkcesozluk.game.GuessResult
import com.hayhak.turkcesozluk.game.WordleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameUiStateTest {

    private fun guess(word: String, target: String) =
        GuessResult(word.toList(), WordleEngine.evaluate(word, target))

    private fun state(
        target: String = "teali",
        guesses: List<GuessResult> = emptyList(),
        slots: List<Char?> = List(target.length) { null },
        revealed: Set<Int> = emptySet(),
    ) = GameUiState(
        status = GameStatus.PLAYING,
        wordLength = target.length,
        guesses = guesses,
        slots = slots,
        revealedPositions = revealed,
    )

    @Test
    fun `nothing is locked before the first guess`() {
        assertTrue(state().lockedPositions.isEmpty())
    }

    @Test
    fun `gifted positions are locked`() {
        assertEquals(setOf(1, 2), state(revealed = setOf(1, 2)).lockedPositions)
    }

    @Test
    fun `positions proven correct by a guess are locked too`() {
        // Ekran görüntüsündeki durum: hedef "teali", tahmin "keali" -> E A L İ yerinde.
        val locked = state(guesses = listOf(guess("keali", "teali"))).lockedPositions
        assertEquals(setOf(1, 2, 3, 4), locked)
        assertFalse(0 in locked)
    }

    @Test
    fun `gifted and proven positions are locked together`() {
        val locked = state(
            guesses = listOf(guess("keali", "teali")),
            revealed = setOf(1, 2),
        ).lockedPositions
        assertEquals(setOf(1, 2, 3, 4), locked)
    }

    @Test
    fun `a letter that is in the word but misplaced is not locked`() {
        // "teali" içindeki 'a' var ama 0. sırada değil: kilitlenmemeli.
        val locked = state(guesses = listOf(guess("aksiz", "teali"))).lockedPositions
        assertTrue(locked.isEmpty())
    }

    @Test
    fun `the cursor sits on the first empty slot`() {
        assertEquals(0, state().activeIndex)
        assertEquals(3, state(slots = listOf('t', 'e', 'a', null, null)).activeIndex)
        // Kilitli harfler dolu geldiğinde imleç ilk boşluğa atlar.
        assertEquals(0, state(slots = listOf(null, 'e', 'a', 'l', 'i')).activeIndex)
    }

    @Test
    fun `a full row reports itself complete and has no cursor`() {
        val full = state(slots = "teali".toList())
        assertTrue(full.isRowComplete)
        assertEquals(-1, full.activeIndex)
    }

    @Test
    fun `an empty or partial row is not complete`() {
        assertFalse(state().isRowComplete)
        assertFalse(state(slots = listOf('t', 'e', null, null, null)).isRowComplete)
        // Hiç tur başlamamışken de tamam sayılmamalı.
        assertFalse(GameUiState().isRowComplete)
    }

    @Test
    fun `attempts left counts down with each guess`() {
        assertEquals(WordleEngine.MAX_ATTEMPTS, state().attemptsLeft)
        val two = state(guesses = listOf(guess("keali", "teali"), guess("beali", "teali")))
        assertEquals(WordleEngine.MAX_ATTEMPTS - 2, two.attemptsLeft)
    }
}
