package com.hayhak.turkcesozluk.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.data.db.DictionaryMode
import com.hayhak.turkcesozluk.game.LetterState
import com.hayhak.turkcesozluk.game.WordleEngine
import com.hayhak.turkcesozluk.util.uppercaseTR
import com.hayhak.turkcesozluk.viewmodel.GameStatus
import com.hayhak.turkcesozluk.viewmodel.GameUiState
import com.hayhak.turkcesozluk.viewmodel.GameViewModel

private val CorrectGreen = Color(0xFF10B981)
private val PresentAmber = Color(0xFFF59E0B)

private val KeyboardRows = listOf(
    "abcçdefgğh",
    "ıijklmnoöp",
    "rsştuüvyz",
)

@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val shakeSignal by viewModel.shakeSignal.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeSignal) {
        if (shakeSignal > 0) {
            repeat(4) { i ->
                shakeOffset.animateTo(if (i % 2 == 0) 8f else -8f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    when (state.status) {
        GameStatus.IDLE -> GameIntro(
            currentMode = currentMode,
            onModeChange = viewModel::setMode,
            onStart = viewModel::startGame,
        )
        GameStatus.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        GameStatus.NO_WORD -> GameUnavailable(
            currentMode = currentMode,
            onModeChange = viewModel::setMode,
            onRetry = viewModel::startGame,
        )
        else -> GameBoard(
            state = state,
            currentMode = currentMode,
            onModeChange = viewModel::setMode,
            shakeOffsetPx = shakeOffset.value,
            onLetter = viewModel::onLetter,
            onDelete = viewModel::onDelete,
            onSubmit = viewModel::onSubmit,
            onHint = viewModel::revealHint,
            onNewWord = viewModel::startGame,
        )
    }
}

@Composable
private fun GameIntro(
    currentMode: DictionaryMode,
    onModeChange: (DictionaryMode) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameModeSelector(
            currentMode = currentMode,
            onModeChange = onModeChange,
            modifier = Modifier.align(Alignment.End),
        )
        Spacer(Modifier.height(12.dp))
        Icon(
            Icons.Rounded.EmojiEvents,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.game_intro_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.game_intro_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.game_hint_explainer, WordleEngine.HINTS_PER_ROUND),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        LegendRow()
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.heightIn(min = 52.dp),
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.game_start),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

/**
 * Oyunun hangi sözlükten kelime çekeceğini kullanıcı seçer. Kip değişimi
 * uygulama genelindeki sözlük kipini değiştirir (veri o kipten yüklenir).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameModeSelector(
    currentMode: DictionaryMode,
    onModeChange: (DictionaryMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = dictionaryModeTitle(currentMode),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(R.string.settings_dictionary_mode),
                    modifier = Modifier.size(18.dp),
                )
            },
            shape = RoundedCornerShape(12.dp),
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DictionaryMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(dictionaryModeTitle(mode)) },
                    onClick = {
                        expanded = false
                        onModeChange(mode)
                    },
                    leadingIcon = {
                        RadioButton(selected = mode == currentMode, onClick = null)
                    },
                )
            }
        }
    }
}

/**
 * Etiketler dile göre epey uzayabildiğinden (ör. Almanca "Kommt im Wort vor")
 * sabit bir Row dar ekranlarda taşar; akan satır kullanıyoruz.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegendRow() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LegendItem(CorrectGreen, stringResource(R.string.game_legend_correct))
        LegendItem(PresentAmber, stringResource(R.string.game_legend_present))
        LegendItem(MaterialTheme.colorScheme.outline, stringResource(R.string.game_legend_absent))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun GameUnavailable(
    currentMode: DictionaryMode,
    onModeChange: (DictionaryMode) -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameModeSelector(
            currentMode = currentMode,
            onModeChange = onModeChange,
            modifier = Modifier.align(Alignment.End),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.game_no_word),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(16.dp)) {
            Text(stringResource(R.string.game_new_word))
        }
    }
}

@Composable
private fun GameBoard(
    state: GameUiState,
    currentMode: DictionaryMode,
    onModeChange: (DictionaryMode) -> Unit,
    shakeOffsetPx: Float,
    onLetter: (Char) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    onHint: () -> Unit,
    onNewWord: () -> Unit,
) {
    val finished = state.status == GameStatus.WON || state.status == GameStatus.LOST
    val gridScroll = rememberScrollState()

    // Alçak ekranlarda 6 satırlık tablo klavyeyle birlikte sığmayabiliyor; hem elle
    // kaydırılabilsin hem de sıradaki satır kendiliğinden görünür kalsın istiyoruz.
    LaunchedEffect(state.guesses.size, gridScroll.maxValue) {
        val max = gridScroll.maxValue
        if (max > 0) {
            val fraction = state.guesses.size.toFloat() / WordleEngine.MAX_ATTEMPTS
            gridScroll.animateScrollTo((max * fraction).toInt().coerceIn(0, max))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GameModeSelector(
            currentMode = currentMode,
            onModeChange = onModeChange,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 16.dp, top = 8.dp),
        )
        ClueCard(state)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(gridScroll)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GuessGrid(state, shakeOffsetPx)
        }

        if (finished) {
            ResultPanel(state = state, onNewWord = onNewWord)
        } else {
            Keyboard(
                keyboard = state.keyboard,
                disabledKeys = state.disabledKeys,
                hintsLeft = state.hintsLeft,
                hintEnabled = state.hintAvailable && state.hintsLeft > 0,
                onLetter = onLetter,
                onDelete = onDelete,
                onSubmit = onSubmit,
                onHint = onHint,
            )
        }
    }
}

@Composable
private fun ClueCard(state: GameUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.game_clue_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                if (state.solvedCount > 0) {
                    Text(
                        text = stringResource(R.string.game_solved_count, state.solvedCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = state.clue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.game_letter_count, state.wordLength) +
                    "  ·  " +
                    stringResource(R.string.game_attempts_left, state.attemptsLeft),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GuessGrid(state: GameUiState, shakeOffsetPx: Float) {
    if (state.wordLength <= 0) return

    val lockedPositions = state.lockedPositions

    BoxWithConstraints {
        val gap = 6.dp
        val available = maxWidth - gap * (state.wordLength - 1)
        val cell = (available / state.wordLength).coerceAtMost(56.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            repeat(WordleEngine.MAX_ATTEMPTS) { row ->
                val isActiveRow = row == state.guesses.size && state.status == GameStatus.PLAYING
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    modifier = if (isActiveRow) Modifier.offset(x = shakeOffsetPx.dp) else Modifier,
                ) {
                    repeat(state.wordLength) { column ->
                        val guess = state.guesses.getOrNull(row)
                        val gifted = column in state.revealedPositions
                        val locked = column in lockedPositions
                        when {
                            guess != null -> LetterCell(
                                letter = guess.letters[column],
                                state = guess.states[column],
                                size = cell,
                            )
                            isActiveRow -> LetterCell(
                                letter = state.slots.getOrNull(column),
                                // Kilitli harf (hediye ya da yeri kesinleşmiş) zaten doğru.
                                state = if (locked) LetterState.CORRECT else null,
                                size = cell,
                                highlighted = column == state.activeIndex,
                                gifted = gifted,
                            )
                            else -> LetterCell(letter = null, state = null, size = cell)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterCell(
    letter: Char?,
    state: LetterState?,
    size: androidx.compose.ui.unit.Dp,
    highlighted: Boolean = false,
    /** Kullanıcının kendi bulmadığı, hediye edilen harf. */
    gifted: Boolean = false,
) {
    val background = when (state) {
        LetterState.CORRECT -> CorrectGreen
        LetterState.PRESENT -> PresentAmber
        LetterState.ABSENT -> MaterialTheme.colorScheme.outline
        null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val contentColor = if (state == null) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }
    val border = when {
        // Hediye harf, kullanıcının bulduğu harflerden ayırt edilebilsin.
        gifted -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        state != null -> null
        highlighted -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(10.dp),
        color = background,
        border = border,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter?.toString()?.uppercaseTR().orEmpty(),
                color = contentColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.42f).sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ResultPanel(state: GameUiState, onNewWord: () -> Unit) {
    val won = state.status == GameStatus.WON
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(if (won) R.string.game_won_title else R.string.game_lost_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (won) CorrectGreen else MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.game_correct_answer, state.revealedAnswer),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            if (!won && state.savedToMistakes) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.game_saved_to_mistakes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onNewWord,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    stringResource(R.string.game_new_word),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun Keyboard(
    keyboard: Map<Char, LetterState>,
    disabledKeys: Set<Char>,
    hintsLeft: Int,
    hintEnabled: Boolean,
    onLetter: (Char) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    onHint: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            KeyboardRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEach { letter ->
                        LetterKey(
                            letter = letter,
                            state = keyboard[letter],
                            enabled = letter !in disabledKeys,
                            modifier = Modifier.weight(1f),
                            onClick = { onLetter(letter) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onHint,
                    enabled = hintEnabled,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    Icon(
                        Icons.Rounded.Redeem,
                        contentDescription = stringResource(R.string.cd_game_hint),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = hintsLeft.toString(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = stringResource(R.string.cd_game_delete),
                    )
                }
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    Text(
                        stringResource(R.string.game_submit),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterKey(
    letter: Char,
    state: LetterState?,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val background = when (state) {
        LetterState.CORRECT -> CorrectGreen
        LetterState.PRESENT -> PresentAmber
        LetterState.ABSENT -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        null -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (state) {
        LetterState.CORRECT, LetterState.PRESENT -> Color.White
        LetterState.ABSENT -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        null -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(8.dp),
        color = background,
        enabled = enabled,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter.toString().uppercaseTR(),
                // Pasif tuş, rengini koruyup soluklaşsın: hangi harfin neden
                // kapandığı (yeşil/gri) görünür kalmalı.
                color = if (enabled) contentColor else contentColor.copy(alpha = 0.45f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
            )
        }
    }
}
