package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.QuizQuestion
import com.hayhak.turkcesozluk.util.capitalizeTR
import javax.inject.Inject
import javax.inject.Singleton

private const val OPTION_COUNT = 4

@Singleton
class QuizRepository @Inject constructor() {

    /**
     * Soru köküne yalnızca "ana" kelimeler uygundur. cachedKeys ters eşlemeleri
     * (yani anlam cümlelerini) de içerdiğinden burada primaryKeys kullanılır.
     * Veri henüz hazır değilse veya kelimenin karşılığı yoksa null döner.
     */
    fun getRandomWord(): Pair<String, String>? {
        val keys = SynonymDataStore.primaryKeys
        if (keys.isEmpty()) return null
        val randomKey = keys.random()
        val answer = SynonymDataStore.synonymMap[randomKey]?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: return null
        return randomKey to answer
    }

    fun getRandomDistractors(count: Int, excluding: Set<String>): List<String> {
        val pool = SynonymDataStore.cachedMeanings
        if (pool.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        var tries = 0
        val maxTries = (count * 20).coerceAtMost(pool.size * 4)
        while (result.size < count && tries < maxTries) {
            val candidate = pool.random()
            if (candidate.isNotBlank() && candidate !in excluding && candidate !in result) {
                result.add(candidate)
            }
            tries++
        }
        return result
    }

    /**
     * Şıkları büyük harfe çevirdikten sonra çakışma olabileceğinden (ör. iki farklı
     * kayıt aynı metne normalize olursa) benzersizlik son adımda doğrulanır; tam
     * [OPTION_COUNT] şık üretilemezse soru atlanır ve null dönülür.
     */
    fun generateQuestion(): QuizQuestion? {
        val (word, correct) = getRandomWord() ?: return null

        val distractors = getRandomDistractors(OPTION_COUNT - 1, excluding = setOf(word, correct))
        if (distractors.size < OPTION_COUNT - 1) return null

        val correctLabel = correct.capitalizeTR()
        val options = LinkedHashSet<String>()
        options.add(correctLabel)
        distractors.forEach { options.add(it.capitalizeTR()) }
        if (options.size < OPTION_COUNT) return null

        return QuizQuestion(
            word = word.capitalizeTR(),
            correctAnswer = correctLabel,
            options = options.toList().shuffled()
        )
    }
}
