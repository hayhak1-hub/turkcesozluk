package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.data.model.WordDefinition
import com.hayhak.turkcesozluk.util.normalizeTR
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefinitionRepository @Inject constructor() {

    fun search(query: String): WordDefinition? {
        return DefinitionDataStore.definitionMap[query.normalizeTR()]
    }

    fun getSuggestions(query: String, limit: Int = 7): List<String> {
        if (query.isBlank()) return emptyList()
        val normalized = query.normalizeTR()
        return DefinitionDataStore.cachedKeys.asSequence()
            .filter { it.startsWith(normalized) }
            .take(limit)
            .toList()
    }

    fun getRandomWord(): String {
        val keys = DefinitionDataStore.cachedKeys
        return if (keys.isNotEmpty()) keys.random() else ""
    }

    val wordCount: Int get() = DefinitionDataStore.definitionMap.size
}
