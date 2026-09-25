package com.hayhak.turkcesozluk.game

/** Bir tahmindeki tek bir harfin durumu. */
enum class LetterState {
    /** Harf doğru ve doğru yerde. */
    CORRECT,

    /** Harf kelimede var ama başka bir yerde. */
    PRESENT,

    /** Harf kelimede (kalan adette) yok. */
    ABSENT,
}

/** Değerlendirilmiş tek bir tahmin satırı. */
data class GuessResult(
    val letters: List<Char>,
    val states: List<LetterState>,
)

/**
 * Wordle kurallarının saf (Android'e bağımsız) uygulaması.
 *
 * Tüm giriş/çıkış Türkçe kurallarına göre küçük harfe indirgenmiş metin bekler;
 * büyük harfe çevirme yalnızca gösterim katmanının işidir.
 */
object WordleEngine {

    const val MAX_ATTEMPTS = 6

    /** Tur başına kullanıcıya hediye edilebilecek harf sayısı. */
    const val HINTS_PER_ROUND = 2

    /** Türk alfabesi (29 harf), klavye düzeni ve kelime süzme için tek kaynak. */
    const val ALPHABET = "abcçdefgğhıijklmnoöprsştuüvyz"

    private val alphabetSet = ALPHABET.toSet()

    /** Yalnızca Türk alfabesindeki harflerden oluşan, verilen uzunluk aralığındaki kelimeler oynanabilir. */
    fun isPlayableWord(word: String, minLength: Int, maxLength: Int): Boolean {
        if (word.length !in minLength..maxLength) return false
        return word.all { it in alphabetSet }
    }

    /**
     * Tahmini hedefe göre değerlendirir.
     *
     * Tekrar eden harfler Wordle'daki gibi "adet" mantığıyla işlenir: önce yerinde
     * olanlar işaretlenip hedefteki adetten düşülür, kalan adet yetmiyorsa fazladan
     * tekrarlar [LetterState.ABSENT] olur. Örn. hedef "kaya", tahmin "aaaa" ise
     * yalnızca 2. ve 4. 'a' işaretlenir.
     */
    fun evaluate(guess: String, target: String): List<LetterState> {
        require(guess.length == target.length) {
            "Tahmin ve hedef aynı uzunlukta olmalı: ${guess.length} != ${target.length}"
        }

        val states = MutableList(guess.length) { LetterState.ABSENT }
        val remaining = HashMap<Char, Int>(target.length * 2)

        // 1. geçiş: yerinde olanlar
        for (i in target.indices) {
            if (guess[i] == target[i]) {
                states[i] = LetterState.CORRECT
            } else {
                remaining[target[i]] = (remaining[target[i]] ?: 0) + 1
            }
        }

        // 2. geçiş: yeri yanlış ama kelimede olanlar
        for (i in guess.indices) {
            if (states[i] == LetterState.CORRECT) continue
            val count = remaining[guess[i]] ?: 0
            if (count > 0) {
                states[i] = LetterState.PRESENT
                remaining[guess[i]] = count - 1
            }
        }

        return states
    }

    fun isWin(states: List<LetterState>): Boolean =
        states.isNotEmpty() && states.all { it == LetterState.CORRECT }

    /**
     * Klavyede her harfin gösterileceği durum: aynı harf birden çok tahminde
     * geçtiyse en iyi (en bilgilendirici) durum kazanır.
     */
    fun keyboardStates(results: List<GuessResult>): Map<Char, LetterState> {
        val best = HashMap<Char, LetterState>()
        results.forEach { result ->
            result.letters.forEachIndexed { index, letter ->
                val state = result.states[index]
                val current = best[letter]
                if (current == null || rank(state) > rank(current)) {
                    best[letter] = state
                }
            }
        }
        return best
    }

    /**
     * Önceki tahminlerden yeri kesin olarak bilinen konumlar. Harf hediyesi bu
     * konumları tekrar açıp hakkı boşa harcamasın diye kullanılır.
     */
    fun knownCorrectPositions(results: List<GuessResult>): Set<Int> {
        val known = mutableSetOf<Int>()
        results.forEach { result ->
            result.states.forEachIndexed { index, state ->
                if (state == LetterState.CORRECT) known.add(index)
            }
        }
        return known
    }

    /**
     * Harf hediyesiyle açılabilecek konumlar: yeri tahminlerden zaten bilinenler ve
     * daha önce hediye edilenler elenir. Boş dönerse açılacak bir şey kalmamıştır.
     */
    fun hintCandidates(
        wordLength: Int,
        guesses: List<GuessResult>,
        alreadyRevealed: Set<Int>,
    ): List<Int> {
        val known = knownCorrectPositions(guesses)
        return (0 until wordLength).filter { it !in known && it !in alreadyRevealed }
    }

    /**
     * Klavyede basılmasının işe yaramayacağı harfler:
     *  - kelimede olmadığı kesinleşenler (gri),
     *  - kelimedeki bütün kopyaları zaten kilitli konumlara oturmuş olanlar.
     *
     * İkinci kural olmadan "her doğru harfi kilitle" demek, aynı harfin iki kez
     * geçtiği kelimelerde (ör. "kaya", "teneke") oyunu oynanamaz hale getirirdi.
     */
    fun unusableKeys(
        target: String,
        lockedPositions: Set<Int>,
        keyboard: Map<Char, LetterState>,
    ): Set<Char> {
        val absent = keyboard.filterValues { it == LetterState.ABSENT }.keys
        val placed = lockedPositions.groupingBy { target[it] }.eachCount()
        val exhausted = placed.filterKeys { letter ->
            placed.getValue(letter) >= target.count { it == letter }
        }.keys
        return absent + exhausted
    }

    private fun rank(state: LetterState): Int = when (state) {
        LetterState.ABSENT -> 0
        LetterState.PRESENT -> 1
        LetterState.CORRECT -> 2
    }
}
