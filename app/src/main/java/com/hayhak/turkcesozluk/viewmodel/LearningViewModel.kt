package com.hayhak.turkcesozluk.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.turkcesozluk.data.db.FavoriteDao
import com.hayhak.turkcesozluk.data.db.FavoriteWord
import com.hayhak.turkcesozluk.data.db.UserStatsManager
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
        // Otomatik başlatma kaldırıldı
        isLoading = false
    }

    fun startLearning() {
        viewModelScope.launch {
            isLoading = true
            val favorites = favoriteDao.getAllFavorites().first()
            if (favorites.isNotEmpty()) {
                cards = favorites.shuffled()
                currentCardIndex = 0
                isFlipped = false
                isLearningActive = true
            }
            isLoading = false
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
