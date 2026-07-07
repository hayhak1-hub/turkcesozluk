package com.hayhak.esanlamli.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.esanlamli.QuizQuestion
import com.hayhak.esanlamli.data.db.UserStatsManager
import com.hayhak.esanlamli.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    application: Application,
    private val quizRepository: QuizRepository
) : AndroidViewModel(application) {
    
    var score by mutableIntStateOf(0)
    var correctAnswers by mutableIntStateOf(0)
    var wrongAnswers by mutableIntStateOf(0)
    var currentQuestion by mutableStateOf<QuizQuestion?>(null)
    var totalQuestionsAsked by mutableIntStateOf(0)
    
    var selectedDuration by mutableIntStateOf(60)
    var timeLeft by mutableIntStateOf(60)
    var isGameOver by mutableStateOf(false)
    var isGameStarted by mutableStateOf(false)
    
    val currentMode = com.hayhak.esanlamli.data.db.SettingsManager.modeState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS)

    private var timerJob: Job? = null
    private val askedQuestionWords = mutableSetOf<String>()

    private fun startTimer() {
        timerJob?.cancel()
        timeLeft = selectedDuration
        isGameOver = false
        
        if (selectedDuration == 0) return // Sonsuz mod, zamanlayıcıyı başlatma

        timerJob = viewModelScope.launch {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isGameOver = true
            UserStatsManager.updateStreak(getApplication())
        }
    }

    fun checkAnswer(isCorrect: Boolean) {
        if (isGameOver) return
        if (isCorrect) {
            score += 10
            correctAnswers++
        } else {
            wrongAnswers++
        }
        totalQuestionsAsked++
        if (isCorrect) {
            viewModelScope.launch {
                UserStatsManager.updateStreak(getApplication())
            }
        }
    }

    fun nextQuestion() {
        if (isGameOver) return
        
        var newQuestion: QuizQuestion
        var attempts = 0
        do {
            newQuestion = quizRepository.generateQuestion()
            attempts++
        } while (askedQuestionWords.contains(newQuestion.word) && (attempts < 10))
        
        askedQuestionWords.add(newQuestion.word)
        currentQuestion = newQuestion
        
        if (askedQuestionWords.size > 100) {
            askedQuestionWords.clear()
        }
    }

    fun startNewGame() {
        score = 0
        correctAnswers = 0
        wrongAnswers = 0
        totalQuestionsAsked = 0
        askedQuestionWords.clear()
        isGameOver = false
        isGameStarted = true
        nextQuestion()
        startTimer()
    }
    
    fun quitGame() {
        timerJob?.cancel()
        isGameStarted = false
        isGameOver = false
    }
}
