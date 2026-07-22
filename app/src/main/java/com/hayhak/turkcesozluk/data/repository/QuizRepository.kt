package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.QuizQuestion
import com.hayhak.turkcesozluk.util.capitalizeTR
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor() {

    fun getRandomWord(): Pair<String, String> {
        if (SynonymDataStore.cachedKeys.isEmpty()) return "Kelime" to "Yükleniyor"
        val randomKey = SynonymDataStore.cachedKeys.random()
        return randomKey to (SynonymDataStore.synonymMap[randomKey]?.firstOrNull() ?: "")
    }

    fun getRandomDistractors(count: Int, excluding: Set<String>): List<String> {
        val pool = SynonymDataStore.cachedMeanings
        if (pool.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        var tries = 0
        val maxTries = pool.size.coerceAtMost(200)
        while (result.size < count && tries < maxTries) {
            val candidate = pool.random()
            if (candidate !in excluding && candidate !in result) {
                result.add(candidate)
            }
            tries++
        }
        return result
    }

    fun generateQuestion(): QuizQuestion {
        val pair = getRandomWord()
        val word = pair.first
        val correct = pair.second
        val distractors = getRandomDistractors(3, excluding = setOf(word, correct))

        val options = (distractors + correct).shuffled().map { it.capitalizeTR() }

        return QuizQuestion(
            word = word.capitalizeTR(),
            correctAnswer = correct.capitalizeTR(),
            options = options
        )
    }
}
