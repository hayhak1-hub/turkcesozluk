package com.hayhak.esanlamli.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.esanlamli.data.db.FavoriteDao
import com.hayhak.esanlamli.data.db.FavoriteWord
import com.hayhak.esanlamli.data.db.UserStatsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    application: Application,
    private val favoriteDao: FavoriteDao
) : AndroidViewModel(application) {

    private var cards = emptyList<FavoriteWord>()
    var currentCardIndex by mutableIntStateOf(0)
    var isFlipped by mutableStateOf(false)
    var isLearningActive by mutableStateOf(false)
    var isLoading by mutableStateOf(true)

    init {
        loadFavoritesAndStart()
    }

    private fun loadFavoritesAndStart() {
        viewModelScope.launch {
            isLoading = true
            val favorites = favoriteDao.getAllFavorites().first()
            if (favorites.isNotEmpty()) {
                cards = favorites.shuffled()
                isLearningActive = true
            } else {
                isLearningActive = false
            }
            isLoading = false
            currentCardIndex = 0
            isFlipped = false
        }
    }

    fun stopLearning() {
        isLearningActive = false
    }

    fun nextCard() {
        if (currentCardIndex < cards.size - 1) {
            currentCardIndex++
            isFlipped = false
        } else {
            stopLearning()
        }
    }

    fun flipCard() {
        isFlipped = !isFlipped
        if (isFlipped) {
            viewModelScope.launch {
                UserStatsManager.updateStreak(getApplication())
            }
        }
    }

    fun getCurrentCard(): FavoriteWord? = if (cards.isNotEmpty()) cards[currentCardIndex] else null

    fun getProgress(): Float = if (cards.isNotEmpty()) (currentCardIndex + 1).toFloat() / cards.size else 0f
}
