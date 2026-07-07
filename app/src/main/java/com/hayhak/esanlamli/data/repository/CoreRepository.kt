package com.hayhak.esanlamli.data.repository

import com.hayhak.esanlamli.data.db.UserSynonymDao
import com.hayhak.esanlamli.util.normalizeTR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreRepository @Inject constructor(
    private val csvLoader: CsvLoader,
    private val userSynonymDao: UserSynonymDao
) {
    private val mutex = Mutex()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    suspend fun initialize() {
        loadDefinitionsIfNeeded()
        mutex.withLock {
            // SettingsManager değişimlerini takip et
            com.hayhak.esanlamli.data.db.SettingsManager.modeState.collect { mode ->
                _isInitialized.value = false
                loadData(mode)
                _isInitialized.value = true
            }
        }
    }

    private suspend fun loadDefinitionsIfNeeded() {
        if (DefinitionDataStore.definitionMap.isNotEmpty()) return
        try {
            DefinitionDataStore.definitionMap.putAll(csvLoader.loadDefinitions())
            DefinitionDataStore.updateCache()
        } catch (e: Exception) {
            android.util.Log.e("CoreRepository", "Error loading definitions", e)
        }
    }

    private suspend fun loadData(mode: com.hayhak.esanlamli.data.db.DictionaryMode) {
        try {
            SynonymDataStore.synonymMap.clear()

            // 1. Assets'den yükle
            val loadResult = csvLoader.loadFromAssets(mode)
            SynonymDataStore.synonymMap.putAll(loadResult.data)
            val primaryWords = loadResult.primaryWords.toMutableSet()

            // 2. Room'dan kullanıcı kelimelerini yükle
            try {
                val userWords = userSynonymDao.getAllUserSynonyms().first()
                userWords.forEach { userSynonym ->
                    val w = userSynonym.word.normalizeTR()
                    val s = userSynonym.synonym.normalizeTR()
                    if (w.isNotEmpty() && s.isNotEmpty()) {
                        primaryWords.add(w)
                        val setW = SynonymDataStore.synonymMap.getOrPut(w) { emptySet() }.toMutableSet()
                        setW.add(s)
                        SynonymDataStore.synonymMap[w] = setW.toSet()

                        val setS = SynonymDataStore.synonymMap.getOrPut(s) { emptySet() }.toMutableSet()
                        setS.add(w)
                        SynonymDataStore.synonymMap[s] = setS.toSet()
                    }
                }
            } catch (e: Exception) {
                // Ignore or log
            }

            SynonymDataStore.updateCache(primaryWords)
        } catch (e: Exception) {
            // Critical error
        }
    }
}
