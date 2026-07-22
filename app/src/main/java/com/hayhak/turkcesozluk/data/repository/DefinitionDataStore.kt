package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.data.model.WordDefinition
import java.util.concurrent.ConcurrentHashMap

object DefinitionDataStore {
    val definitionMap = ConcurrentHashMap<String, WordDefinition>()

    @Volatile
    var cachedKeys: List<String> = emptyList()

    fun updateCache() {
        cachedKeys = definitionMap.keys.toList().sorted()
    }
}
