package com.hayhak.turkcesozluk.game

import com.hayhak.turkcesozluk.game.LetterState.ABSENT
import com.hayhak.turkcesozluk.game.LetterState.CORRECT
import com.hayhak.turkcesozluk.game.LetterState.PRESENT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordleEngineTest {

    @Test
    fun `an exact guess is all correct`() {
        assertEquals(
            listOf(CORRECT, CORRECT, CORRECT, CORRECT),
            WordleEngine.evaluate("kaya", "kaya")
        )
        assertTrue(WordleEngine.isWin(WordleEngine.evaluate("kaya", "kaya")))
    }

    @Test
    fun `letters in the wrong place are marked present`() {
        // hedef "kaya", tahmin "ayak": tüm harfler var ama hiçbiri yerinde değil
        assertEquals(
            listOf(PRESENT, PRESENT, PRESENT, PRESENT),
            WordleEngine.evaluate("ayak", "kaya")
        )
    }

    @Test
    fun `letters missing from the target are absent`() {
        assertEquals(
            listOf(ABSENT, ABSENT, ABSENT, ABSENT),
            WordleEngine.evaluate("demi", "kaya")
        )
    }

    @Test
    fun `an extra copy of a letter is absent once the target runs out`() {
        // hedef "kitap" içinde tek 'a' var: iki 'a'lı tahminde yalnızca ilki işaretlenir.
        assertEquals(
            listOf(PRESENT, ABSENT, ABSENT, ABSENT, ABSENT),
            WordleEngine.evaluate("aabbb", "kitap")
        )
    }

    @Test
    fun `extra copies are absent even when one of them is in the right place`() {
        // 'a' yalnızca 4. sırada doğru; kalan dört 'a' fazladan olduğu için ABSENT.
        val states = WordleEngine.evaluate("aaaaa", "kitap")
        assertEquals(listOf(ABSENT, ABSENT, ABSENT, CORRECT, ABSENT), states)
    }

    @Test
    fun `a correct placement is preferred over an earlier present`() {
        // hedef "elma", tahmin "aaaa" -> yalnızca son 'a' yerinde
        val states = WordleEngine.evaluate("aaaa", "elma")
        assertEquals(listOf(ABSENT, ABSENT, ABSENT, CORRECT), states)
    }

    @Test
    fun `Turkish specific letters are handled as distinct letters`() {
        // 'ı' ile 'i' ayrı harflerdir; birbirinin yerine sayılmamalı.
        assertEquals(listOf(ABSENT, ABSENT, ABSENT, ABSENT), WordleEngine.evaluate("iiii", "ışık"))
        val states = WordleEngine.evaluate("ışık", "ışık")
        assertTrue(WordleEngine.isWin(states))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `guessing a different length is rejected`() {
        WordleEngine.evaluate("kay", "kaya")
    }

    @Test
    fun `isWin is false for a partial match`() {
        assertFalse(WordleEngine.isWin(WordleEngine.evaluate("kava", "kaya")))
        assertFalse(WordleEngine.isWin(emptyList()))
    }

    @Test
    fun `keyboard keeps the best state seen for each letter`() {
        val first = GuessResult("ayak".toList(), WordleEngine.evaluate("ayak", "kaya"))
        val second = GuessResult("kaya".toList(), WordleEngine.evaluate("kaya", "kaya"))

        val afterFirst = WordleEngine.keyboardStates(listOf(first))
        assertEquals(PRESENT, afterFirst['k'])

        // İkinci tahminde 'k' yerinde çıktı: klavye artık daha iyi durumu göstermeli.
        val afterSecond = WordleEngine.keyboardStates(listOf(first, second))
        assertEquals(CORRECT, afterSecond['k'])
    }

    @Test
    fun `keyboard never downgrades a letter that was already correct`() {
        val correctGuess = GuessResult("kaya".toList(), WordleEngine.evaluate("kaya", "kaya"))
        // Bu tahminde 'k' yalnızca PRESENT; daha iyi olan CORRECT korunmalı.
        val weakerGuess = GuessResult("akaa".toList(), WordleEngine.evaluate("akaa", "kaya"))
        assertEquals(PRESENT, WordleEngine.keyboardStates(listOf(weakerGuess))['k'])
        assertEquals(CORRECT, WordleEngine.keyboardStates(listOf(correctGuess, weakerGuess))['k'])
        assertEquals(CORRECT, WordleEngine.keyboardStates(listOf(weakerGuess, correctGuess))['k'])
    }

    @Test
    fun `known correct positions are collected across guesses`() {
        // hedef "kaya": ilk tahminde 'k' (0) yerinde, ikincide 'y' (2) yerinde
        val first = GuessResult("kava".toList(), WordleEngine.evaluate("kava", "kaya"))
        val second = GuessResult("saya".toList(), WordleEngine.evaluate("saya", "kaya"))

        assertEquals(setOf(0, 1, 3), WordleEngine.knownCorrectPositions(listOf(first)))
        assertEquals(setOf(0, 1, 2, 3), WordleEngine.knownCorrectPositions(listOf(first, second)))
    }

    @Test
    fun `no positions are known before the first guess`() {
        assertTrue(WordleEngine.knownCorrectPositions(emptyList()).isEmpty())
        // Hiçbiri yerinde olmayan bir tahmin de konum bilgisi vermez.
        val miss = GuessResult("ayak".toList(), WordleEngine.evaluate("ayak", "kaya"))
        assertTrue(WordleEngine.knownCorrectPositions(listOf(miss)).isEmpty())
    }

    @Test
    fun `every position can be gifted before the first guess`() {
        assertEquals(
            listOf(0, 1, 2, 3),
            WordleEngine.hintCandidates(wordLength = 4, guesses = emptyList(), alreadyRevealed = emptySet())
        )
    }

    @Test
    fun `a gift never reveals a position the player already knows`() {
        // "kava" -> 0, 1 ve 3 yerinde; geriye yalnızca 2 kalır.
        val guess = GuessResult("kava".toList(), WordleEngine.evaluate("kava", "kaya"))
        assertEquals(
            listOf(2),
            WordleEngine.hintCandidates(4, listOf(guess), emptySet())
        )
    }

    @Test
    fun `a gift never repeats an already gifted position`() {
        assertEquals(
            listOf(0, 2),
            WordleEngine.hintCandidates(4, emptyList(), alreadyRevealed = setOf(1, 3))
        )
    }

    @Test
    fun `no candidates remain once the whole word is known`() {
        val solved = GuessResult("kaya".toList(), WordleEngine.evaluate("kaya", "kaya"))
        assertTrue(WordleEngine.hintCandidates(4, listOf(solved), emptySet()).isEmpty())
        assertTrue(WordleEngine.hintCandidates(4, emptyList(), setOf(0, 1, 2, 3)).isEmpty())
    }

    @Test
    fun `known positions and gifts are excluded together`() {
        val guess = GuessResult("kava".toList(), WordleEngine.evaluate("kava", "kaya"))
        // 0,1,3 tahminden biliniyor; 2 de hediye edilmişti -> geriye hiçbir şey kalmaz.
        assertTrue(WordleEngine.hintCandidates(4, listOf(guess), setOf(2)).isEmpty())
    }

    @Test
    fun `letters proven absent are disabled on the keyboard`() {
        val guess = GuessResult("demi".toList(), WordleEngine.evaluate("demi", "kaya"))
        val disabled = WordleEngine.unusableKeys(
            target = "kaya",
            lockedPositions = emptySet(),
            keyboard = WordleEngine.keyboardStates(listOf(guess)),
        )
        assertEquals(setOf('d', 'e', 'm', 'i'), disabled)
    }

    @Test
    fun `a letter is disabled once all of its copies are placed`() {
        // "kitap" içinde tek 'k' var ve yeri kilitlendi -> artık basılamaz.
        val disabled = WordleEngine.unusableKeys("kitap", setOf(0), emptyMap())
        assertEquals(setOf('k'), disabled)
    }

    @Test
    fun `a repeated letter stays usable while a copy is still missing`() {
        // "kaya" iki 'a' içerir; yalnızca biri kilitliyken 'a' basılabilir kalmalı.
        assertTrue(WordleEngine.unusableKeys("kaya", setOf(1), emptyMap()).isEmpty())
        // Her iki 'a' da yerine oturunca kapanır.
        assertEquals(setOf('a'), WordleEngine.unusableKeys("kaya", setOf(1, 3), emptyMap()))
    }

    @Test
    fun `absent and exhausted letters are disabled together`() {
        val guess = GuessResult("demi".toList(), WordleEngine.evaluate("demi", "kaya"))
        val disabled = WordleEngine.unusableKeys(
            target = "kaya",
            lockedPositions = setOf(0),
            keyboard = WordleEngine.keyboardStates(listOf(guess)),
        )
        assertEquals(setOf('d', 'e', 'm', 'i', 'k'), disabled)
    }

    @Test
    fun `nothing is disabled at the start of a round`() {
        assertTrue(WordleEngine.unusableKeys("kaya", emptySet(), emptyMap()).isEmpty())
    }

    @Test
    fun `alphabet has the 29 Turkish letters and no q w x`() {
        assertEquals(29, WordleEngine.ALPHABET.length)
        assertEquals(29, WordleEngine.ALPHABET.toSet().size)
        listOf('q', 'w', 'x').forEach { assertFalse(it in WordleEngine.ALPHABET) }
        listOf('ç', 'ğ', 'ı', 'i', 'ö', 'ş', 'ü').forEach { assertTrue(it in WordleEngine.ALPHABET) }
    }

    @Test
    fun `playable words are single Turkish words within the length range`() {
        assertTrue(WordleEngine.isPlayableWord("kitap", 4, 7))
        assertTrue(WordleEngine.isPlayableWord("ışık", 4, 7))
        // çok kısa / çok uzun
        assertFalse(WordleEngine.isPlayableWord("ev", 4, 7))
        assertFalse(WordleEngine.isPlayableWord("kütüphaneci", 4, 7))
        // boşluklu deyim, kesme işareti, rakam ve alfabe dışı harf
        assertFalse(WordleEngine.isPlayableWord("abandone etmek", 4, 7))
        assertFalse(WordleEngine.isPlayableWord("ankara'da", 4, 7))
        assertFalse(WordleEngine.isPlayableWord("web22", 4, 7))
        assertFalse(WordleEngine.isPlayableWord("wxqz", 4, 7))
    }
}
