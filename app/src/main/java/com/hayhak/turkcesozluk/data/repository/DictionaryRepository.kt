package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.util.capitalizeTR
import com.hayhak.turkcesozluk.util.normalizeTR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepository @Inject constructor() {

    fun getSynonym(query: String): String? {
        val normalized = query.normalizeTR()
        return SynonymDataStore.synonymMap[normalized]?.joinToString(", ")
    }

    fun getSynonyms(query: String): List<String> {
        val normalized = query.normalizeTR()
        return SynonymDataStore.synonymMap[normalized]?.toList() ?: emptyList()
    }

    fun getSuggestions(query: String, limit: Int = 5): List<String> {
        if (query.isBlank()) return emptyList()
        val normalized = query.normalizeTR()
        return SynonymDataStore.cachedKeys.asSequence()
            .filter { it.startsWith(normalized) }
            .take(limit)
            .toList()
    }

    /** Veri henüz hazır değilse null döner; metni çağıran katman yerelleştirir. */
    fun getDailyWord(): Pair<String, String>? {
        if (SynonymDataStore.primaryKeys.isEmpty()) return null
        val calendar = Calendar.getInstance()
        val seed = (calendar.get(Calendar.YEAR) * 1000) + calendar.get(Calendar.DAY_OF_YEAR)
        val index = seed % SynonymDataStore.primaryKeys.size
        val word = SynonymDataStore.primaryKeys[index]
        return word.capitalizeTR() to
               (SynonymDataStore.synonymMap[word]?.firstOrNull()?.capitalizeTR() ?: "")
    }

    fun getWordTree(query: String): Map<String, List<String>> {
        val normalized = query.normalizeTR()
        val tree = mutableMapOf<String, List<String>>()
        val level1 = SynonymDataStore.synonymMap[normalized]?.toList() ?: emptyList()
        if (level1.isNotEmpty()) {
            tree[normalized] = level1
            level1.take(5).forEach { synonym ->
                // Sadece kısa kelimelerin ağaçta alt dalları olsun (açıklama cümlelerinin değil)
                if (synonym.split(" ").size <= 3) {
                    val level2 = SynonymDataStore.synonymMap[synonym]?.filter { it != normalized } ?: emptyList()
                    if (level2.isNotEmpty()) tree[synonym] = level2.take(3)
                }
            }
        }
        return tree
    }

    /**
     * updateCache() burada ön-sıralı liste veremediği için on binlerce anahtarı
     * yeniden sıralar; ana thread'i bloklamamak adına arka plana alınır.
     */
    suspend fun addWordManual(word: String, synonym: String) = withContext(Dispatchers.Default) {
        val w = word.normalizeTR()
        val s = synonym.normalizeTR()
        if (w.isEmpty() || s.isEmpty()) return@withContext

        SynonymDataStore.addSynonym(w, s)
        SynonymDataStore.addSynonym(s, w)

        // Kullanıcı kelimelerini de primary olarak ekle
        val updatedPrimary = SynonymDataStore.primaryKeys.toMutableSet()
        updatedPrimary.add(w)
        SynonymDataStore.updateCache(updatedPrimary)
    }

    suspend fun addWordsManual(words: List<Pair<String, String>>) = withContext(Dispatchers.Default) {
        val primary = SynonymDataStore.primaryKeys.toMutableSet()
        words.forEach { (word, answer) ->
            val w = word.normalizeTR()
            val s = answer.normalizeTR()
            if (w.isNotEmpty() && s.isNotEmpty()) {
                SynonymDataStore.addSynonym(w, s)
                SynonymDataStore.addSynonym(s, w)
                primary.add(w)
            }
        }
        if (words.isNotEmpty()) SynonymDataStore.updateCache(primary)
    }

    /** Veri henüz hazır değilse null döner. */
    fun getRandomWord(): Pair<String, String>? {
        val keys = SynonymDataStore.primaryKeys
        if (keys.isEmpty()) return null
        val randomKey = keys.random()
        return randomKey to (SynonymDataStore.synonymMap[randomKey]?.firstOrNull() ?: "")
    }

    val synonymsCount: Int get() = SynonymDataStore.synonymMap.size
}
