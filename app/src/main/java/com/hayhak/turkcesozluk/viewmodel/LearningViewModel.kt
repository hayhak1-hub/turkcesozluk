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
import com.hayhak.turkcesozluk.data.db.AppDatabase
import com.hayhak.turkcesozluk.data.db.StudyRecord
import com.hayhak.turkcesozluk.data.db.ReviewSchedule

@HiltViewModel
class LearningViewModel @Inject constructor(
    application: Application,
    private val favoriteDao: FavoriteDao,
    private val database: AppDatabase
) : AndroidViewModel(application) {

    private var cards = emptyList<FavoriteWord>()
    var currentCardIndex by mutableIntStateOf(0)
    var isFlipped by mutableStateOf(false)
    var isLearningActive by mutableStateOf(false)
    var isLoading by mutableStateOf(true)
    var nothingDue by mutableStateOf(false)
    var saving by mutableStateOf(false)

    init {
        // Otomatik başlatma kaldırıldı
        isLoading = false
    }

    fun startLearning(dueOnly: Boolean = false) {
        if (isLoading || saving) return
        isLoading = true
        viewModelScope.launch {
            try {
            val favorites = favoriteDao.getAllFavorites().first()
            val records = database.studyDao().all().filter { it.kind == "review" }
            val now = System.currentTimeMillis()
            cards = favorites.filter { favorite -> !dueOnly || records.none {
                it.word == favorite.word && it.answer == favorite.synonym && it.dueAt > now
            } }.shuffled()
            nothingDue = cards.isEmpty()
            if (cards.isNotEmpty()) {
                currentCardIndex = 0
                isFlipped = false
                isLearningActive = true
            }
            } finally { isLoading = false }
        }
    }

    fun stopLearning() {
        isLearningActive = false
    }

    fun rateCard(known: Boolean) {
        val card = getCurrentCard() ?: return
        if (!isFlipped || saving) return
        saving = true
        viewModelScope.launch {
            try {
                val old = database.studyDao().all().firstOrNull {
                    it.kind == "review" && it.word == card.word && it.answer == card.synonym
                }
                val now = System.currentTimeMillis()
                val (streak, due) = ReviewSchedule.next(old?.streak ?: 0, known, now)
                database.studyDao().put(StudyRecord("review", "", card.word, card.synonym,
                    streak = streak, dueAt = due, updatedAt = now))
                nextCard()
            } finally { saving = false }
        }
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
