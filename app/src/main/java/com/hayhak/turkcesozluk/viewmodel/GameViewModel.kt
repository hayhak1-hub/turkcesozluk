package com.hayhak.turkcesozluk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.turkcesozluk.data.db.AppDatabase
import com.hayhak.turkcesozluk.data.db.SettingsManager
import com.hayhak.turkcesozluk.data.db.StudyRecord
import com.hayhak.turkcesozluk.data.db.UserStatsManager
import com.hayhak.turkcesozluk.data.db.DictionaryMode
import com.hayhak.turkcesozluk.data.repository.CoreRepository
import com.hayhak.turkcesozluk.data.repository.GameRepository
import com.hayhak.turkcesozluk.data.repository.GameTarget
import com.hayhak.turkcesozluk.game.GuessResult
import com.hayhak.turkcesozluk.game.LetterState
import com.hayhak.turkcesozluk.game.WordleEngine
import com.hayhak.turkcesozluk.util.capitalizeTR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

enum class GameStatus { IDLE, LOADING, PLAYING, WON, LOST, NO_WORD }

data class GameUiState(
    val status: GameStatus = GameStatus.IDLE,
    val clue: String = "",
    val wordLength: Int = 0,
    /** Yalnızca tur bittiğinde dolar; oyun sürerken cevabı sızdırmaz. */
    val revealedAnswer: String = "",
    val guesses: List<GuessResult> = emptyList(),
    /**
     * Aktif satır: her konum ya boş (null) ya da bir harf. Sabit uzunlukta olması,
     * hediye edilen harfin doğru konuma kilitlenebilmesi için gerekli.
     */
    val slots: List<Char?> = emptyList(),
    /** Harf hediyesiyle açılan konumlar; kullanıcı bunları silemez. */
    val revealedPositions: Set<Int> = emptySet(),
    val hintsLeft: Int = WordleEngine.HINTS_PER_ROUND,
    val hintAvailable: Boolean = false,
    val keyboard: Map<Char, LetterState> = emptyMap(),
    /** Basılması işe yaramayacağı için pasifleştirilen klavye tuşları. */
    val disabledKeys: Set<Char> = emptySet(),
    val solvedCount: Int = 0,
    val savedToMistakes: Boolean = false,
) {
    val attemptsLeft: Int get() = WordleEngine.MAX_ATTEMPTS - guesses.size

    /**
     * Değiştirilemeyen konumlar: harf hediyesiyle açılanlar ve yeri önceki
     * tahminlerden kesinleşenler. Kullanıcı üç satır boyunca doğruladığı bir harfi
     * tekrar tekrar yazmak zorunda kalmasın diye bunlar taşınır ve kilitlenir.
     */
    val lockedPositions: Set<Int>
        get() = revealedPositions + WordleEngine.knownCorrectPositions(guesses)

    /** İmlecin bulunduğu (ilk boş) konum; satır doluysa -1. */
    val activeIndex: Int get() = slots.indexOfFirst { it == null }

    val isRowComplete: Boolean get() = slots.isNotEmpty() && slots.none { it == null }
}

@HiltViewModel
class GameViewModel @Inject constructor(
    application: Application,
    private val gameRepository: GameRepository,
    private val coreRepository: CoreRepository,
    private val database: AppDatabase,
) : AndroidViewModel(application) {

    /** Oyunun hangi sözlükten kelime çektiği; kullanıcı oyun ekranından değiştirebilir. */
    val currentMode = SettingsManager.modeState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsManager.modeState.value)

    init {
        // Kip değişince sözlük verisi baştan yüklenir; yeni kelime ancak yükleme
        // bittikten sonra seçilebilir. Devam eden bir tur varsa kendiliğinden
        // yeni kipten bir kelimeyle başlatılır.
        viewModelScope.launch {
            SettingsManager.modeState.drop(1).collect {
                playedWords.clear()
                gameRepository.invalidate()
                if (_uiState.value.status != GameStatus.IDLE) {
                    _uiState.update { state -> state.copy(status = GameStatus.LOADING) }
                    coreRepository.isInitialized.first { ready -> ready }
                    startGame(force = true)
                }
            }
        }
    }

    fun setMode(mode: DictionaryMode) {
        if (mode == SettingsManager.modeState.value) return
        SettingsManager.setDictionaryMode(getApplication(), mode)
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    /** Tahmin eksik harfli gönderildiğinde ekranın titreşim/uyarı tetiklemesi için sayaç. */
    private val _shakeSignal = MutableStateFlow(0)
    val shakeSignal = _shakeSignal.asStateFlow()

    private var target: GameTarget? = null
    private var sessionMode: String = SettingsManager.modeState.value.name
    private val playedWords = mutableSetOf<String>()

    fun startGame() = startGame(force = false)

    private fun startGame(force: Boolean) {
        if (!force && _uiState.value.status == GameStatus.LOADING) return
        _uiState.update { it.copy(status = GameStatus.LOADING) }

        viewModelScope.launch {
            sessionMode = SettingsManager.modeState.value.name
            // Oynanabilir kelimeleri süzmek on binlerce kaydı gezebilir; ana thread'de olmaz.
            val next = withContext(Dispatchers.Default) {
                gameRepository.newTarget(excluding = playedWords)
            }

            if (next == null) {
                target = null
                _uiState.update { GameUiState(status = GameStatus.NO_WORD, solvedCount = it.solvedCount) }
                return@launch
            }

            target = next
            playedWords.add(next.word)
            if (playedWords.size > 200) playedWords.clear()

            _uiState.update {
                GameUiState(
                    status = GameStatus.PLAYING,
                    clue = next.clue.capitalizeTR(),
                    wordLength = next.word.length,
                    slots = List(next.word.length) { null },
                    hintsLeft = WordleEngine.HINTS_PER_ROUND,
                    hintAvailable = true,
                    solvedCount = it.solvedCount,
                )
            }
        }
    }

    fun onLetter(letter: Char) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        val index = state.activeIndex
        if (index < 0) return
        _uiState.update {
            it.copy(slots = it.slots.toMutableList().also { slots -> slots[index] = letter })
        }
    }

    fun onDelete() {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        // Kilitli harfler (hediye + yeri kesinleşmiş) silinemez.
        val locked = state.lockedPositions
        val index = state.slots.indices.lastOrNull {
            state.slots[it] != null && it !in locked
        } ?: return
        _uiState.update {
            it.copy(slots = it.slots.toMutableList().also { slots -> slots[index] = null })
        }
    }

    /**
     * Bir harfi hediye eder: henüz bilinmeyen bir konumu açar ve o konumu kilitler.
     * Boş konumlar önceliklidir ki kullanıcının yazdığı harfler boşuna silinmesin.
     */
    fun revealHint() {
        val state = _uiState.value
        val current = target ?: return
        if (state.status != GameStatus.PLAYING || state.hintsLeft <= 0) return

        val candidates = WordleEngine.hintCandidates(
            wordLength = state.wordLength,
            guesses = state.guesses,
            alreadyRevealed = state.revealedPositions,
        )
        if (candidates.isEmpty()) {
            _uiState.update { it.copy(hintAvailable = false) }
            return
        }

        val index = candidates.filter { state.slots[it] == null }.ifEmpty { candidates }.random()
        val letter = current.word[index]
        val revealed = state.revealedPositions + index
        val hintsLeft = state.hintsLeft - 1

        val nextKeyboard = state.keyboard + (letter to LetterState.CORRECT)
        val nextLocked = revealed + WordleEngine.knownCorrectPositions(state.guesses)

        _uiState.update {
            it.copy(
                slots = it.slots.toMutableList().also { slots -> slots[index] = letter },
                revealedPositions = revealed,
                hintsLeft = hintsLeft,
                hintAvailable = hintsLeft > 0 && candidates.size > 1,
                keyboard = nextKeyboard,
                disabledKeys = WordleEngine.unusableKeys(current.word, nextLocked, nextKeyboard),
            )
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        val current = target ?: return
        if (state.status != GameStatus.PLAYING) return

        if (!state.isRowComplete) {
            _shakeSignal.update { it + 1 }
            return
        }

        val guess = state.slots.filterNotNull().joinToString("")
        val states = WordleEngine.evaluate(guess, current.word)
        val result = GuessResult(guess.toList(), states)
        val guesses = state.guesses + result
        val won = WordleEngine.isWin(states)
        val lost = !won && guesses.size >= WordleEngine.MAX_ATTEMPTS

        // Hediye harfler ve bu tahminle birlikte yeri kesinleşen harfler sonraki
        // satıra taşınır; oyuncu bildiği harfleri yeniden yazmak zorunda kalmaz.
        val nextLocked = state.revealedPositions + WordleEngine.knownCorrectPositions(guesses)
        val nextSlots = List(state.wordLength) { index ->
            if (index in nextLocked) current.word[index] else null
        }
        val revealedLetters = state.revealedPositions.associate { current.word[it] to LetterState.CORRECT }
        val stillHintable = WordleEngine
            .hintCandidates(state.wordLength, guesses, state.revealedPositions)
            .isNotEmpty()

        val nextKeyboard = WordleEngine.keyboardStates(guesses) + revealedLetters

        _uiState.update {
            it.copy(
                guesses = guesses,
                slots = nextSlots,
                keyboard = nextKeyboard,
                disabledKeys = WordleEngine.unusableKeys(current.word, nextLocked, nextKeyboard),
                status = when {
                    won -> GameStatus.WON
                    lost -> GameStatus.LOST
                    else -> GameStatus.PLAYING
                },
                hintAvailable = it.hintsLeft > 0 && stillHintable,
                revealedAnswer = if (won || lost) current.word.capitalizeTR() else "",
                solvedCount = if (won) it.solvedCount + 1 else it.solvedCount,
            )
        }

        if (won) onWin(current) else if (lost) onLoss(current)
    }

    private fun onWin(current: GameTarget) {
        viewModelScope.launch {
            UserStatsManager.updateStreak(getApplication())
            // Daha önce bu kelimede yanılmışsa artık biliyor; tekrar listesinden düşsün.
            database.studyDao().delete(current.toStudyRecord(sessionMode))
        }
    }

    private fun onLoss(current: GameTarget) {
        viewModelScope.launch {
            database.studyDao().put(
                current.toStudyRecord(sessionMode).copy(updatedAt = System.currentTimeMillis())
            )
            _uiState.update { it.copy(savedToMistakes = true) }
        }
    }

    private fun GameTarget.toStudyRecord(mode: String) = StudyRecord(
        kind = "mistake",
        mode = mode,
        word = word.capitalizeTR(),
        answer = clue.capitalizeTR(),
        options = JSONArray(quizOptions).toString(),
    )
}
