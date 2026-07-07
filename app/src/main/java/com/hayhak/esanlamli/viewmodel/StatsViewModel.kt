package com.hayhak.esanlamli.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.esanlamli.data.billing.PremiumManager
import com.hayhak.esanlamli.data.db.FavoriteDao
import com.hayhak.esanlamli.data.db.SearchHistoryDao
import com.hayhak.esanlamli.data.db.UserSynonymDao
import com.hayhak.esanlamli.data.db.UserStatsManager
import com.hayhak.esanlamli.data.repository.DictionaryRepository
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
    private val userSynonymDao: UserSynonymDao,
    private val premiumManager: PremiumManager
) : AndroidViewModel(application) {

    val isPremium = premiumManager.isPremium
    val premiumPriceText = premiumManager.priceText

    fun purchasePremium(activity: Activity) {
        premiumManager.launchPurchaseFlow(activity)
    }

    val favorites = favoriteDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val recentSearches = historyDao.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val userWords = userSynonymDao.getAllUserSynonyms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val synonymsCount = dictionaryRepository.synonymsCount
    
    private val _weeklyStats = MutableStateFlow<List<Int>>(emptyList())
    val weeklyStats = _weeklyStats.asStateFlow()

    val currentTheme = com.hayhak.esanlamli.data.db.SettingsManager.themeState

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _weeklyStats.value = UserStatsManager.getWeeklyStats(getApplication())
        }
    }

    fun setTheme(theme: com.hayhak.esanlamli.data.db.AppTheme) {
        com.hayhak.esanlamli.data.db.SettingsManager.setTheme(getApplication(), theme)
    }
}
