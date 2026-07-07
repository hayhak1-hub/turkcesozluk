package com.hayhak.esanlamli.data.repository

import com.hayhak.esanlamli.util.capitalizeTR
import com.hayhak.esanlamli.util.normalizeTR
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

    fun getDailyWord(): Pair<String, String> {
        if (SynonymDataStore.primaryKeys.isEmpty()) return "Sözlük" to "Hazırlanıyor"
        val calendar = Calendar.getInstance()
        val seed = (calendar.get(Calendar.YEAR) * 1000) + calendar.get(Calendar.DAY_OF_YEAR)
        val index = seed % SynonymDataStore.primaryKeys.size
        val word = SynonymDataStore.primaryKeys[index]
        return word.capitalizeTR() to 
               (SynonymDataStore.synonymMap[word]?.first()?.capitalizeTR() ?: "")
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

    fun addWordManual(word: String, synonym: String) {
        val w = word.normalizeTR()
        val s = synonym.normalizeTR()
        if (w.isEmpty() || s.isEmpty()) return

        val setW = SynonymDataStore.synonymMap.getOrPut(w) { emptySet() }.toMutableSet()
        setW.add(s)
        SynonymDataStore.synonymMap[w] = setW.toSet()

        val setS = SynonymDataStore.synonymMap.getOrPut(s) { emptySet() }.toMutableSet()
        setS.add(w)
        SynonymDataStore.synonymMap[s] = setS.toSet()

        // Kullanıcı kelimelerini de primary olarak ekle
        val updatedPrimary = SynonymDataStore.primaryKeys.toMutableSet()
        updatedPrimary.add(w)
        SynonymDataStore.updateCache(updatedPrimary)
    }

    fun getRandomWord(): Pair<String, String> {
        if (SynonymDataStore.primaryKeys.isEmpty()) return "Kelime" to "Yükleniyor"
        val randomKey = SynonymDataStore.primaryKeys.random()
        return randomKey to (SynonymDataStore.synonymMap[randomKey]?.first() ?: "")
    }

    val synonymsCount: Int get() = SynonymDataStore.synonymMap.size
}
