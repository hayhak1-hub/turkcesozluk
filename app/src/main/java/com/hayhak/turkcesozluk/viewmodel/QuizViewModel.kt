package com.hayhak.turkcesozluk.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.turkcesozluk.QuizQuestion
import com.hayhak.turkcesozluk.data.db.UserStatsManager
import com.hayhak.turkcesozluk.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.hayhak.turkcesozluk.data.db.AppDatabase
import com.hayhak.turkcesozluk.data.db.StudyRecord
import com.hayhak.turkcesozluk.data.db.SettingsManager
import kotlinx.coroutines.flow.first
import org.json.JSONArray

@HiltViewModel
class QuizViewModel @Inject constructor(
    application: Application,
    private val quizRepository: QuizRepository,
    private val database: AppDatabase
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
    
    val currentMode = com.hayhak.turkcesozluk.data.db.SettingsManager.modeState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS)

    private var timerJob: Job? = null
    private var answerWriteJob: Job? = null
    var mistakesOnly by mutableStateOf(false)
    var starting by mutableStateOf(false)
    var startError by mutableStateOf(false)
    private var answered = false
    private var sessionMode = SettingsManager.modeState.value.name
    private val mistakes = ArrayDeque<StudyRecord>()
    private var activeMistake: StudyRecord? = null
    val savedMistakes = database.studyDao().observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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
        if (isGameOver || answered || currentQuestion == null) return
        answered = true
        val question = currentQuestion!!
        val record = activeMistake ?: StudyRecord("mistake", sessionMode, question.word,
            question.correctAnswer, JSONArray(question.options).toString())
        answerWriteJob = viewModelScope.launch {
            if (isCorrect) database.studyDao().delete(record)
            else database.studyDao().put(record.copy(updatedAt = System.currentTimeMillis()))
        }
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
        answered = false
        if (mistakesOnly) {
            // Bozuk/boş şıklı kayıtları atla; kuyruk bitince bitir.
            while (true) {
                val record = mistakes.removeFirstOrNull()
                if (record == null) {
                    timerJob?.cancel()
                    if (currentQuestion == null) {
                        // Hiç soru üretilemedi (hepsi bozuktu)
                    } else {
                        isGameOver = true
                    }
                    return
                }
                val options = runCatching {
                    val json = JSONArray(record.options)
                    List(json.length()) { json.getString(it) }
                }.getOrDefault(emptyList())
                if (options.isEmpty()) continue
                activeMistake = record
                currentQuestion = QuizQuestion(
                    record.word,
                    record.answer,
                    options.shuffled()
                )
                return
            }
        }

        // generateQuestion(), yeterli şık üretemediğinde null döner; tekrar eden
        // kelimelerden kaçınmak için de birkaç deneme yapıyoruz.
        var question: QuizQuestion? = null
        for (attempt in 0 until 10) {
            val candidate = quizRepository.generateQuestion() ?: continue
            if (question == null) question = candidate
            if (candidate.word !in askedQuestionWords) {
                question = candidate
                break
            }
        }

        if (question == null) return
        askedQuestionWords.add(question.word)
        currentQuestion = question

        if (askedQuestionWords.size > 100) {
            askedQuestionWords.clear()
        }
    }

    fun startNewGame(practiceMistakes: Boolean = mistakesOnly) {
        if (starting) return
        starting = true
        mistakesOnly = practiceMistakes
        viewModelScope.launch {
        try {
        answerWriteJob?.join()
        sessionMode = SettingsManager.modeState.value.name
        mistakes.clear()
        if (practiceMistakes) {
            mistakes.addAll(
                database.studyDao().all()
                    .filter { it.kind == "mistake" && it.mode == sessionMode }
                    .shuffled()
            )
        }
        if (practiceMistakes && mistakes.isEmpty()) {
            quitGame()
            startError = true
            return@launch
        }
        startError = false
        activeMistake = null
        currentQuestion = null
        score = 0
        correctAnswers = 0
        wrongAnswers = 0
        totalQuestionsAsked = 0
        askedQuestionWords.clear()
        isGameOver = false
        isGameStarted = true
        nextQuestion()
        if (currentQuestion == null) {
            quitGame()
            startError = practiceMistakes
            return@launch
        }
        startTimer()
        } finally { starting = false }
        }
    }
    
    fun quitGame() {
        timerJob?.cancel()
        isGameStarted = false
        isGameOver = false
    }
}
