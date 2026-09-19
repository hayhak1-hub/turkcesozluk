package com.hayhak.turkcesozluk.data.repository

import java.util.concurrent.ConcurrentHashMap

object SynonymDataStore {
    private val _synonymMap = ConcurrentHashMap<String, Set<String>>()
    val synonymMap: Map<String, Set<String>> get() = _synonymMap
    
    @Volatile
    var cachedKeys: List<String> = emptyList() // Arama önerileri için tüm kelimeler
    
    @Volatile
    private var _cachedMeanings: List<String>? = null // Quiz distractor'ları için tüm anlamlar (lazy)

    val cachedMeanings: List<String>
        get() = _cachedMeanings ?: synchronized(this) {
            _cachedMeanings ?: _synonymMap.values.asSequence()
                .flatten()
                .distinct()
                .toList()
                .also { _cachedMeanings = it }
        }

    @Volatile
    var primaryKeys: List<String> = emptyList() // Sadece "Şansını Dene" ve "Günün Kelimesi" için temiz kelimeler

    fun clear() {
        _synonymMap.clear()
        _cachedMeanings = null
    }

    fun putAll(data: Map<String, Set<String>>) {
        _synonymMap.putAll(data)
        _cachedMeanings = null
    }

    /**
     * ConcurrentHashMap tek basina "oku-degistir-yaz" dizisini korumaz; es zamanli
     * ekleme yapilirsa guncelleme kaybolabilir. compute()/merge() API 24 istedigi
     * icin (minSdk 23) putIfAbsent/replace ile CAS dongusu kullaniyoruz.
     */
    fun addSynonym(word: String, synonym: String) {
        while (true) {
            val existing = _synonymMap[word]
            if (existing == null) {
                if (_synonymMap.putIfAbsent(word, setOf(synonym)) == null) break
            } else {
                if (synonym in existing) break
                if (_synonymMap.replace(word, existing, existing + synonym)) break
            }
        }
        _cachedMeanings = null
    }

    fun updateCache(
        primaryWords: Set<String>,
        preSortedKeys: List<String>? = null,
        preSortedPrimary: List<String>? = null
    ) {
        // Eğer preSorted listeler verilmişse ve Map boyutu ile uyuşuyorsa onları kullanırız.
        // Uyuşmuyorsa (kullanıcı yeni kelime eklemişse) yeniden sıralarız.
        if (preSortedKeys != null && preSortedKeys.size == _synonymMap.size) {
            cachedKeys = preSortedKeys
        } else {
            cachedKeys = _synonymMap.keys.toList().sorted()
        }

        if (preSortedPrimary != null && preSortedPrimary.size == primaryWords.size) {
            primaryKeys = preSortedPrimary
        } else {
            primaryKeys = primaryWords.toList().sorted()
        }
        
        // cachedMeanings artık lazy yüklendiği için burada hesaplamıyoruz, açılışı hızlandırır.
    }
}
