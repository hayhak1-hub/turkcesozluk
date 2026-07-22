package com.hayhak.turkcesozluk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.turkcesozluk.data.db.FavoriteDao
import com.hayhak.turkcesozluk.data.db.FavoriteWord
import com.hayhak.turkcesozluk.data.db.SearchHistory
import com.hayhak.turkcesozluk.data.db.SearchHistoryDao
import com.hayhak.turkcesozluk.data.db.UserSynonym
import com.hayhak.turkcesozluk.data.db.UserSynonymDao
import com.hayhak.turkcesozluk.data.db.UserStatsManager
import com.hayhak.turkcesozluk.data.model.TdkEntry
import com.hayhak.turkcesozluk.data.model.TdkLookupResult
import com.hayhak.turkcesozluk.data.model.WordDefinition
import com.hayhak.turkcesozluk.data.repository.CoreRepository
import com.hayhak.turkcesozluk.data.repository.DictionaryRepository
import com.hayhak.turkcesozluk.data.repository.TdkRepository
import com.hayhak.turkcesozluk.util.normalizeTR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<String> = emptyList(),
    val wordTree: Map<String, List<String>> = emptyMap(),
    val suggestions: List<String> = emptyList(),
    val definitionResult: WordDefinition? = null,
    val tdkResult: TdkEntry? = null,
    val tdkLoading: Boolean = false,
    val tdkError: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class DictionaryViewModel @Inject constructor(
    application: Application,
    private val coreRepository: CoreRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val tdkRepository: TdkRepository,
    private val favoriteDao: FavoriteDao,
    private val historyDao: SearchHistoryDao,
    private val userSynonymDao: UserSynonymDao
) : AndroidViewModel(application) {

    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState = _searchUiState.asStateFlow()

    val currentMode = com.hayhak.turkcesozluk.data.db.SettingsManager.modeState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS)

    val dictionaryTitle = currentMode.map { mode ->
        when (mode) {
            com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS ->
                application.getString(com.hayhak.turkcesozluk.R.string.dictionary_title_synonyms)
            com.hayhak.turkcesozluk.data.db.DictionaryMode.VERBS ->
                application.getString(com.hayhak.turkcesozluk.R.string.dictionary_title_verbs)
            com.hayhak.turkcesozluk.data.db.DictionaryMode.DEFINITIONS ->
                application.getString(com.hayhak.turkcesozluk.R.string.mode_definitions)
            com.hayhak.turkcesozluk.data.db.DictionaryMode.IDIOMS ->
                application.getString(com.hayhak.turkcesozluk.R.string.mode_idioms)
            com.hayhak.turkcesozluk.data.db.DictionaryMode.ADJECTIVES ->
                application.getString(com.hayhak.turkcesozluk.R.string.mode_adjectives)
            com.hayhak.turkcesozluk.data.db.DictionaryMode.ALL ->
                application.getString(com.hayhak.turkcesozluk.R.string.dictionary_title_all)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val recentSearches = historyDao.getRecentSearches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteWordsSet = favoriteDao.getAllFavorites()
        .map { list -> list.map { it.word.normalizeTR() }.toSet() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val dailyWord = coreRepository.isInitialized
        .filter { it }
        .map { dictionaryRepository.getDailyWord() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            application.getString(com.hayhak.turkcesozluk.R.string.tab_dictionary) to application.getString(com.hayhak.turkcesozluk.R.string.preparing_label)
        )

    private val _streakCount = MutableStateFlow(0)
    val streakCount = _streakCount.asStateFlow()

    val showDailyWord = com.hayhak.turkcesozluk.data.db.SettingsManager.showDailyWordState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val hasSeenModeHint = com.hayhak.turkcesozluk.data.db.SettingsManager.hasSeenModeHintState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun dismissModeHint() {
        com.hayhak.turkcesozluk.data.db.SettingsManager.markModeHintSeen(getApplication())
    }

    val synonymsCount: Int get() = dictionaryRepository.synonymsCount

    private var searchJob: Job? = null

    init {
        _searchUiState
            .map { it.query }
            .filter { it.isNotBlank() }
            .debounce(1000)
            .distinctUntilChanged()
            .onEach { q ->
                if (_searchUiState.value.results.isNotEmpty()) {
                    historyDao.insertSearch(SearchHistory(q.normalizeTR()))
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            _streakCount.value = UserStatsManager.getStreak(getApplication())
        }
    }

    fun updateQuery(newQuery: String) {
        _searchUiState.update {
            it.copy(
                query = newQuery,
                tdkResult = null,
                tdkLoading = false,
                tdkError = null
            )
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.Default) {
            val normalized = newQuery.normalizeTR()
            val synonymList = if (normalized.isBlank()) emptyList()
                              else dictionaryRepository.getSynonyms(normalized)
            val tree = if (synonymList.isNotEmpty()) dictionaryRepository.getWordTree(normalized)
                       else emptyMap()
            val suggestions = if (synonymList.isEmpty() && normalized.isNotBlank())
                                  dictionaryRepository.getSuggestions(normalized)
                              else emptyList()
            _searchUiState.update {
                it.copy(
                    results = synonymList,
                    wordTree = tree,
                    suggestions = suggestions,
                    definitionResult = null,
                    tdkResult = null,
                    tdkLoading = false,
                    tdkError = null
                )
            }
            if (synonymList.isNotEmpty()) {
                UserStatsManager.updateStreak(getApplication())
                _streakCount.value = UserStatsManager.getStreak(getApplication())
                return@launch
            }

            // Yerelde yoksa TDK'ya düş (yazmayı bitirmeyi bekle)
            if (normalized.length < 2) return@launch
            delay(450)
            if (_searchUiState.value.query.normalizeTR() != normalized) return@launch

            _searchUiState.update { it.copy(tdkLoading = true, tdkError = null, tdkResult = null) }
            when (val tdk = tdkRepository.lookup(normalized)) {
                is TdkLookupResult.Success -> {
                    _searchUiState.update {
                        it.copy(tdkLoading = false, tdkResult = tdk.entry, tdkError = null)
                    }
                    historyDao.insertSearch(SearchHistory(normalized))
                    UserStatsManager.updateStreak(getApplication())
                    _streakCount.value = UserStatsManager.getStreak(getApplication())
                }
                is TdkLookupResult.NotFound -> {
                    _searchUiState.update {
                        it.copy(tdkLoading = false, tdkResult = null, tdkError = null)
                    }
                }
                is TdkLookupResult.Error -> {
                    _searchUiState.update {
                        it.copy(tdkLoading = false, tdkResult = null, tdkError = tdk.message)
                    }
                }
            }
        }
    }

    fun getRandomWord(): Pair<String, String> = dictionaryRepository.getRandomWord()

    fun addNewWord(word: String, synonym: String) {
        viewModelScope.launch {
            val w = word.normalizeTR()
            val s = synonym.normalizeTR()
            userSynonymDao.insertUserSynonym(UserSynonym(word = w, synonym = s))
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
