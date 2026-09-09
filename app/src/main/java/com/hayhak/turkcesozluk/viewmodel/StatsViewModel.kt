package com.hayhak.turkcesozluk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.turkcesozluk.data.db.FavoriteDao
import com.hayhak.turkcesozluk.data.db.SearchHistoryDao
import com.hayhak.turkcesozluk.data.db.UserSynonymDao
import com.hayhak.turkcesozluk.data.db.UserStatsManager
import com.hayhak.turkcesozluk.data.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    application: Application,
    private val dictionaryRepository: DictionaryRepository,
    private val favoriteDao: FavoriteDao,
    private val historyDao: SearchHistoryDao,
    private val userSynonymDao: UserSynonymDao
) : AndroidViewModel(application) {

    val favorites = favoriteDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val recentSearches = historyDao.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val userWords = userSynonymDao.getAllUserSynonyms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val synonymsCount = dictionaryRepository.synonymsCount
    
    private val _weeklyStats = MutableStateFlow<List<Int>>(emptyList())
    val weeklyStats = _weeklyStats.asStateFlow()

    private val _previousWeekTotal = MutableStateFlow(0)
    val previousWeekTotal = _previousWeekTotal.asStateFlow()

    val currentLanguage = com.hayhak.turkcesozluk.data.db.SettingsManager.languageState

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _weeklyStats.value = UserStatsManager.getWeeklyStats(getApplication())
            _previousWeekTotal.value = UserStatsManager.getPreviousWeekTotal(getApplication())
        }
    }

    fun setLanguage(locale: com.hayhak.turkcesozluk.data.db.AppLocale) {
        com.hayhak.turkcesozluk.data.db.SettingsManager.setLanguage(getApplication(), locale)
    }
}
