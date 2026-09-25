package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.game.WordleEngine
import com.hayhak.turkcesozluk.util.capitalizeTR
import javax.inject.Inject
import javax.inject.Singleton

/** Oyunun bir turu: tahmin edilecek kelime, ipucu ve quiz tekrarı için şıklar. */
data class GameTarget(
    val word: String,
    val clue: String,
    /** Yanlış bilindiğinde StudyRecord'a yazılacak 4 şık; üretilemezse boş. */
    val quizOptions: List<String>,
)

const val GAME_MIN_LENGTH = 4
const val GAME_MAX_LENGTH = 7

@Singleton
class GameRepository @Inject constructor(
    private val quizRepository: QuizRepository,
) {

    private var cachedSource: List<String>? = null
    private var cachedPlayable: List<String> = emptyList()

    /** Kip değişiminde önbelleği zorla tazeler. */
    @Synchronized
    fun invalidate() {
        cachedSource = null
        cachedPlayable = emptyList()
    }

    /**
     * Oyuna uygun kelimeler (tek kelime, 4-7 harf, yalnızca Türk alfabesi) tüm
     * sözlüğün küçük bir alt kümesi olduğundan bir kez süzülüp saklanır. Sözlük
     * kipi değişince [SynonymDataStore.primaryKeys] yeni bir liste nesnesiyle
     * değiştiği için referans karşılaştırması önbelleği tazelemeye yeter.
     */
    @Synchronized
    fun playableWords(): List<String> {
        val source = SynonymDataStore.primaryKeys
        if (source !== cachedSource) {
            cachedPlayable = source.filter {
                WordleEngine.isPlayableWord(it, GAME_MIN_LENGTH, GAME_MAX_LENGTH)
            }
            cachedSource = source
        }
        return cachedPlayable
    }

    /**
     * Rastgele bir tur üretir. Bu sözlük kipinde oynanabilir kelime yoksa veya
     * seçilenlerin hiçbirinin kullanılabilir bir karşılığı yoksa null döner.
     */
    fun newTarget(excluding: Set<String> = emptySet()): GameTarget? {
        val words = playableWords()
        if (words.isEmpty()) return null

        // Liste on binlerce kelime olabildiğinden tümünü karıştırmak yerine örnekliyoruz.
        repeat(60) {
            val word = words.random()
            if (word !in excluding) {
                val clue = SynonymDataStore.synonymMap[word]
                    ?.firstOrNull { isUsableClue(it, word) }
                if (clue != null) {
                    return GameTarget(
                        word = word,
                        clue = clue,
                        quizOptions = buildQuizOptions(word, clue),
                    )
                }
            }
        }

        // Hariç tutulanlar yüzünden hiçbir aday kalmadıysa listeyi sıfırdan dene.
        if (excluding.isNotEmpty()) return newTarget()
        return null
    }

    /**
     * İpucu cevabı ele vermemeli: "güzel" kelimesine "çok güzel" ipucu verilirse
     * oyun anlamsızlaşır. Bu yüzden hedef kelimeyi içeren karşılıklar elenir.
     */
    internal fun isUsableClue(clue: String, word: String): Boolean =
        clue.isNotBlank() && !clue.contains(word)

    /**
     * Kaybedilen kelime, quizdeki "sadece yanlışlar" moduyla tekrar edilebilsin diye
     * aynı biçimde 4 şıkla saklanır. Yeterli çeldirici yoksa boş liste döner ve kayıt
     * yine tutulur, yalnızca quizde tekrar edilemez.
     */
    private fun buildQuizOptions(word: String, clue: String): List<String> {
        val distractors = quizRepository.getRandomDistractors(3, excluding = setOf(word, clue))
        if (distractors.size < 3) return emptyList()
        val options = LinkedHashSet<String>()
        options.add(clue.capitalizeTR())
        distractors.forEach { options.add(it.capitalizeTR()) }
        if (options.size < 4) return emptyList()
        return options.toList().shuffled()
    }
}
