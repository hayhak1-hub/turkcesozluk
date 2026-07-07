package com.hayhak.esanlamli.data.repository

import com.hayhak.esanlamli.QuizQuestion
import com.hayhak.esanlamli.util.capitalizeTR
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor() {

    fun getRandomWord(): Pair<String, String> {
        if (SynonymDataStore.cachedKeys.isEmpty()) return "Kelime" to "Yükleniyor"
        val randomKey = SynonymDataStore.cachedKeys.random()
        return randomKey to (SynonymDataStore.synonymMap[randomKey]?.first() ?: "")
    }

    fun getRandomDistractors(count: Int, excluding: Set<String>): List<String> {
        val pool = SynonymDataStore.cachedMeanings.filterNot { it in excluding }
        return if (pool.size <= count) pool.shuffled() else pool.shuffled().take(count)
    }

    fun generateQuestion(): QuizQuestion {
        val pair = getRandomWord()
        val word = pair.first
        val correct = pair.second
        val distractors = getRandomDistractors(3, excluding = setOf(word, correct))
        
        // Şıkları capitalize ederek sunuyoruz
        val options = (distractors + correct).shuffled().map { it.capitalizeTR() }
        
        return QuizQuestion(
            word = word.capitalizeTR(),
            correctAnswer = correct.capitalizeTR(),
            options = options
        )
    }
}
