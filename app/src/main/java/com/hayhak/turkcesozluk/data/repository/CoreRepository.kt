package com.hayhak.turkcesozluk.data.repository

import com.hayhak.turkcesozluk.data.db.UserSynonymDao
import com.hayhak.turkcesozluk.util.normalizeTR
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
        com.hayhak.turkcesozluk.data.db.SettingsManager.modeState.collectLatest { mode ->
            mutex.withLock {
                _isInitialized.value = false
                loadData(mode)
                _isInitialized.value = true
            }
        }
    }

    /**
     * isimler.csv en büyük veri dosyası (~4.7MB); "İsimler" sekmesine hiç girilmezse
     * gereksiz yere açılışı yavaşlatmaması için sadece o sekmeye ilk girişte yüklenir.
     */
    suspend fun ensureDefinitionsLoaded() {
        if (DefinitionDataStore.definitionMap.isNotEmpty()) return
        try {
            DefinitionDataStore.definitionMap.putAll(csvLoader.loadDefinitionsCached())
            DefinitionDataStore.updateCache()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("CoreRepository", "Error loading definitions", e)
        }
    }

    suspend fun ensureInitialized() {
        if (_isInitialized.value) return
        mutex.withLock {
            if (_isInitialized.value) return
            val mode = com.hayhak.turkcesozluk.data.db.SettingsManager.modeState.value
            loadData(mode)
            _isInitialized.value = true
        }
    }

    private suspend fun loadData(mode: com.hayhak.turkcesozluk.data.db.DictionaryMode) {
        val startTime = System.currentTimeMillis()
        android.util.Log.d("CoreRepository", "Loading data for mode: $mode")
        try {
            SynonymDataStore.clear()

            // 1. Assets'den yükle
            val loadResult = csvLoader.loadFromAssetsCached(mode)
            SynonymDataStore.putAll(loadResult.data)
            val primaryWords = loadResult.primaryWords.toMutableSet()
            android.util.Log.d("CoreRepository", "Loaded ${loadResult.data.size} words from assets in ${System.currentTimeMillis() - startTime}ms")

            // 2. Room'dan kullanıcı kelimelerini yükle
            val roomStart = System.currentTimeMillis()
            try {
                val userWords = userSynonymDao.getAllUserSynonyms().first()
                userWords.forEach { userSynonym ->
                    val w = userSynonym.word.normalizeTR()
                    val s = userSynonym.synonym.normalizeTR()
                    if (w.isNotEmpty() && s.isNotEmpty()) {
                        primaryWords.add(w)
                        SynonymDataStore.addSynonym(w, s)
                        SynonymDataStore.addSynonym(s, w)
                    }
                }
                android.util.Log.d("CoreRepository", "Loaded ${userWords.size} user synonyms in ${System.currentTimeMillis() - roomStart}ms")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("CoreRepository", "Error loading user synonyms", e)
            }

            val cacheStart = System.currentTimeMillis()
            SynonymDataStore.updateCache(
                primaryWords,
                loadResult.sortedKeys,
                loadResult.sortedPrimary
            )
            android.util.Log.d("CoreRepository", "SynonymDataStore cache updated in ${System.currentTimeMillis() - cacheStart}ms")
            android.util.Log.d("CoreRepository", "Data loading completed in ${System.currentTimeMillis() - startTime}ms total")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("CoreRepository", "Critical error during loadData", e)
        }
    }
}
