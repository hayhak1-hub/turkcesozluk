package com.hayhak.esanlamli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.esanlamli.data.db.FavoriteDao
import com.hayhak.esanlamli.data.db.FavoriteWord
import com.hayhak.esanlamli.data.db.SearchHistory
import com.hayhak.esanlamli.data.db.SearchHistoryDao
import com.hayhak.esanlamli.data.db.UserSynonym
import com.hayhak.esanlamli.data.db.UserSynonymDao
import com.hayhak.esanlamli.data.db.UserStatsManager
import com.hayhak.esanlamli.data.repository.CoreRepository
import com.hayhak.esanlamli.data.repository.DictionaryRepository
import com.hayhak.esanlamli.util.normalizeTR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class DictionaryViewModel @Inject constructor(
    application: Application,
    private val coreRepository: CoreRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val favoriteDao: FavoriteDao,
    private val historyDao: SearchHistoryDao,
    private val userSynonymDao: UserSynonymDao
) : AndroidViewModel(application) {
    
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results = _results.asStateFlow()

    private val _wordTree = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val wordTree = _wordTree.asStateFlow()

    val currentMode = com.hayhak.esanlamli.data.db.SettingsManager.modeState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS)

    val dictionaryTitle = currentMode.map { mode ->
        if (mode == com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS) {
            application.getString(com.hayhak.esanlamli.R.string.dictionary_title_synonyms)
        } else {
            application.getString(com.hayhak.esanlamli.R.string.dictionary_title_verbs)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val recentSearches = historyDao.getRecentSearches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteWordsSet = favoriteDao.getAllFavorites()
        .map { list -> list.asSequence().map { it.word.normalizeTR() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Günün kelimesi için reactive yapı
    val dailyWord = coreRepository.isInitialized
        .filter { it }
        .map { dictionaryRepository.getDailyWord() }
        .stateIn(
            viewModelScope, 
            SharingStarted.WhileSubscribed(5000), 
            application.getString(com.hayhak.esanlamli.R.string.tab_dictionary) to application.getString(com.hayhak.esanlamli.R.string.preparing_label)
        )
    
    private val _streakCount = MutableStateFlow(0)
    val streakCount = _streakCount.asStateFlow()

    val synonymsCount = dictionaryRepository.synonymsCount

    init {
        // Arama geçmişini güvenli kaydetmek için debounce flow
        _query
            .filter { it.isNotBlank() }
            .debounce(1000)
            .distinctUntilChanged()
            .onEach { q ->
                if (_results.value.isNotEmpty()) {
                    historyDao.insertSearch(SearchHistory(q.normalizeTR()))
                }
            }
            .launchIn(viewModelScope)
            
        // Seri bilgisini yükle
        viewModelScope.launch {
            _streakCount.value = UserStatsManager.getStreak(getApplication())
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        val normalized = newQuery.normalizeTR()
        val synonymList = if (normalized.isBlank()) {
            emptyList()
        } else {
            dictionaryRepository.getSynonyms(normalized)
        }
        _results.value = synonymList
        
        _wordTree.value = if (synonymList.isNotEmpty()) {
            dictionaryRepository.getWordTree(normalized)
        } else {
            emptyMap()
        }
        
        if (synonymList.isNotEmpty()) {
            viewModelScope.launch {
                UserStatsManager.updateStreak(getApplication())
                _streakCount.value = UserStatsManager.getStreak(getApplication())
            }
        }

        _suggestions.value = if (synonymList.isEmpty() && normalized.isNotBlank()) {
            dictionaryRepository.getSuggestions(normalized)
        } else {
            emptyList()
        }
    }

    fun getRandomWord(): Pair<String, String> = dictionaryRepository.getRandomWord()

    fun addNewWord(word: String, synonym: String) {
        viewModelScope.launch {
            val w = word.normalizeTR()
            val s = synonym.normalizeTR()
            val userSynonym = UserSynonym(word = w, synonym = s)
            userSynonymDao.insertUserSynonym(userSynonym)
            dictionaryRepository.addWordManual(w, s)
            UserStatsManager.updateStreak(getApplication())
            _streakCount.value = UserStatsManager.getStreak(getApplication())
            updateQuery(w)
        }
    }

    fun toggleFavorite(word: String, synonym: String, isAlreadyFavorite: Boolean) {
        viewModelScope.launch {
            val w = word.normalizeTR()
            val s = synonym.normalizeTR()
            if (isAlreadyFavorite) {
                favoriteDao.deleteFavorite(FavoriteWord(w, s))
            } else {
                favoriteDao.insertFavorite(FavoriteWord(w, s))
                UserStatsManager.updateStreak(getApplication())
                _streakCount.value = UserStatsManager.getStreak(getApplication())
            }
        }
    }
}
