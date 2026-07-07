package com.hayhak.esanlamli.data.repository

import java.util.concurrent.ConcurrentHashMap

object SynonymDataStore {
    val synonymMap = ConcurrentHashMap<String, Set<String>>()
    
    @Volatile
    var cachedKeys: List<String> = emptyList() // Arama önerileri için tüm kelimeler
    
    @Volatile
    var cachedMeanings: List<String> = emptyList() // Quiz distractor'ları için tüm anlamlar

    @Volatile
    var primaryKeys: List<String> = emptyList() // Sadece "Şansını Dene" ve "Günün Kelimesi" için temiz kelimeler

    fun updateCache(primaryWords: Set<String>) {
        cachedKeys = synonymMap.keys.toList().sorted()
        cachedMeanings = synonymMap.values.flatten().distinct()
        primaryKeys = primaryWords.toList().shuffled()
    }
}
