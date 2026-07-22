package com.hayhak.turkcesozluk.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.turkcesozluk.data.db.FavoriteDao
import com.hayhak.turkcesozluk.data.db.FavoriteWord
import com.hayhak.turkcesozluk.util.normalizeTR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    application: Application,
    private val favoriteDao: FavoriteDao
) : AndroidViewModel(application) {

    var searchQuery by mutableStateOf("")
        private set

    private val _allFavorites = favoriteDao.getAllFavorites()
    
    val favorites: StateFlow<List<FavoriteWord>> = combine(
        _allFavorites,
        snapshotFlow { searchQuery }.debounce(300L)
    ) { list, query ->
        if (query.isBlank()) list
        else {
            val normalized = query.normalizeTR()
            list.filter {
                it.word.normalizeTR().contains(normalized) ||
                it.synonym.normalizeTR().contains(normalized)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun deleteFavorite(favoriteWord: FavoriteWord) {
        viewModelScope.launch {
            favoriteDao.deleteFavorite(favoriteWord)
        }
    }
}
