package com.hayhak.turkcesozluk.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.data.db.DictionaryMode
import com.hayhak.turkcesozluk.ui.components.DictionaryModeMenuButton
import com.hayhak.turkcesozluk.ui.components.GameHintCountMenuButton
import com.hayhak.turkcesozluk.game.LetterState
import com.hayhak.turkcesozluk.game.WordleEngine
import com.hayhak.turkcesozluk.util.uppercaseTR
import com.hayhak.turkcesozluk.viewmodel.GameStatus
import com.hayhak.turkcesozluk.viewmodel.GameUiState
import com.hayhak.turkcesozluk.viewmodel.GameViewModel

internal val CorrectGreen = Color(0xFF10B981)
internal val PresentAmber = Color(0xFFF59E0B)

internal val KeyboardRows = listOf(
    "abcçdefgğh",
    "ıijklmnoöp",
    "rsştuüvyz",
)

@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val shakeSignal by viewModel.shakeSignal.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val hintCount by viewModel.hintCount.collectAsState()

    com.hayhak.turkcesozluk.ui.components.RatePromptEffect(
        trigger = state.status == GameStatus.WON || state.status == GameStatus.LOST
    )

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
            hintCount = hintCount,
            onHintCountChange = viewModel::setHintCount,
            solvedCount = state.solvedCount,
            recordCount = state.recordCount,
            onStart = viewModel::startGame,
        )
        GameStatus.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        GameStatus.NO_WORD -> GameUnavailable(
            currentMode = currentMode,
            onModeChange = viewModel::setMode,
            hintCount = hintCount,
            onHintCountChange = viewModel::setHintCount,
            solvedCount = state.solvedCount,
            recordCount = state.recordCount,
            onRetry = viewModel::startGame,
        )
        else -> GameBoard(
            state = state,
            currentMode = currentMode,
            onModeChange = viewModel::setMode,
            hintCount = hintCount,
            onHintCountChange = viewModel::setHintCount,
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
    hintCount: Int,
    onHintCountChange: (Int) -> Unit,
    solvedCount: Int,
    recordCount: Int,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameSettingsBar(
            currentMode = currentMode,
            onModeChange = onModeChange,
            hintCount = hintCount,
            onHintCountChange = onHintCountChange,
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
        GameScoreRow(solvedCount = solvedCount, recordCount = recordCount)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.game_intro_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
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
            text = stringResource(R.string.game_hint_explainer, hintCount),
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

@Composable
internal fun GameSettingsBar(
    currentMode: DictionaryMode,
    onModeChange: (DictionaryMode) -> Unit,
    hintCount: Int,
    onHintCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GameHintCountMenuButton(
            hintCount = hintCount,
            onHintCountSelected = onHintCountChange,
        )
        DictionaryModeMenuButton(
            currentMode = currentMode,
            onModeSelected = onModeChange,
        )
    }
}

@Composable
internal fun GameScoreRow(
    solvedCount: Int,
    recordCount: Int,
    stacked: Boolean = false,
) {
    val solved = @Composable {
        GameScoreChip(
            text = stringResource(R.string.game_solved_count, solvedCount),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    }
    val record = @Composable {
        GameScoreChip(
            text = stringResource(R.string.game_record_count, recordCount),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Rounded.EmojiEvents,
        )
    }
    if (stacked) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            solved()
            record()
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            solved()
            record()
        }
    }
}

@Composable
private fun GameScoreChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
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
    hintCount: Int,
    onHintCountChange: (Int) -> Unit,
    solvedCount: Int,
    recordCount: Int,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameSettingsBar(
            currentMode = currentMode,
            onModeChange = onModeChange,
            hintCount = hintCount,
            onHintCountChange = onHintCountChange,
            modifier = Modifier.align(Alignment.End),
        )
        Spacer(Modifier.height(24.dp))
        GameScoreRow(solvedCount = solvedCount, recordCount = recordCount)
        Spacer(Modifier.height(16.dp))
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
