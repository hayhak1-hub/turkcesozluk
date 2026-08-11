package com.hayhak.turkcesozluk.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.viewmodel.QuizViewModel

@Composable
fun QuizScreen(viewModel: QuizViewModel = hiltViewModel()) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    val currentQuestion = viewModel.currentQuestion
    val isGameOver = viewModel.isGameOver
    val isGameStarted = viewModel.isGameStarted
    val currentMode by viewModel.currentMode.collectAsState()

    // Mikro Etkileşimler için animasyon değerleri
    val shakeOffset = remember { Animatable(0f) }
    val scoreFlashColor = remember { Animatable(Color.Transparent, Color.VectorConverter(Color.Transparent.colorSpace)) }

    // Yanlış cevapta sallanma efekti
    LaunchedEffect(selectedAnswer) {
        if (selectedAnswer != null && currentQuestion != null && selectedAnswer != currentQuestion.correctAnswer) {
            repeat(4) { i ->
                shakeOffset.animateTo(
                    targetValue = if (i % 2 == 0) 6f else -6f,
                    animationSpec = tween(durationMillis = 50)
                )
            }
            shakeOffset.animateTo(0f)
        }
    }

    // Doğru cevapta puan parlaması
    LaunchedEffect(viewModel.score) {
        if (viewModel.score > 0) {
            scoreFlashColor.animateTo(Color(0xFF10B981).copy(alpha = 0.4f), animationSpec = tween(150))
            scoreFlashColor.animateTo(Color.Transparent, animationSpec = tween(500))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isGameStarted) {
            QuizStartScreen(
                selectedDuration = viewModel.selectedDuration,
                onDurationChange = { viewModel.selectedDuration = it },
                onStart = { viewModel.startNewGame() }
            )
        } else {
            // Oyun Üst Bilgi Çubuğu
            Row(
                modifier = Modifier.fillMaxWidth().offset(x = shakeOffset.value.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.background(scoreFlashColor.value, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        "${stringResource(R.string.quiz_score_label)}: ${viewModel.score}", 
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Timer, 
                        contentDescription = null, 
                        tint = if (viewModel.selectedDuration != 0 && viewModel.timeLeft < 10) Color.Red else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (viewModel.selectedDuration == 0) "∞" else viewModel.timeLeft.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.selectedDuration != 0 && viewModel.timeLeft < 10) Color.Red else MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(onClick = { 
                    viewModel.quitGame()
                    selectedAnswer = null
                }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Sıfırla", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isGameOver) {
                GameOverScreen(
                    score = viewModel.score,
                    correct = viewModel.correctAnswers,
                    wrong = viewModel.wrongAnswers,
                    total = viewModel.totalQuestionsAsked,
                    onRestart = { 
                        viewModel.startNewGame()
                        selectedAnswer = null
                    }
                )
            } else if (currentQuestion != null) {
                AnimatedContent(targetState = currentQuestion, label = "QuestionAnim") { question ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "\"${question.word}\"",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(if (currentMode == com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS) R.string.quiz_question_synonym else R.string.quiz_question_meaning),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                currentQuestion.options.forEach { option ->
                    val isCorrect = option == currentQuestion.correctAnswer
                    val color = when {
                        selectedAnswer == option && isCorrect -> Color(0xFF10B981)
                        selectedAnswer == option && !isCorrect -> MaterialTheme.colorScheme.error
                        selectedAnswer != null && isCorrect -> Color(0xFF10B981)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val contentColor = when {
                        selectedAnswer == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        selectedAnswer == option || isCorrect -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }

                    val scale by animateFloatAsState(if (selectedAnswer == option) 1.05f else 1f, label = "ScaleAnim")

                    Button(
                        onClick = {
                            if (selectedAnswer == null) {
                                selectedAnswer = option
                                viewModel.checkAnswer(isCorrect)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).heightIn(min = 56.dp).scale(scale),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = color, 
                            contentColor = contentColor,
                            disabledContainerColor = color,
                            disabledContentColor = contentColor
                        ),
                        enabled = selectedAnswer == null || selectedAnswer == option || isCorrect
                    ) {
                        Text(
                            option, 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = selectedAnswer != null) {
                    Button(
                        onClick = {
                            viewModel.nextQuestion()
                            selectedAnswer = null
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.quiz_next_question), fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizStartScreen(
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.Timer,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.quiz_duration_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(30, 60, 120, 0).forEach { seconds ->
                FilterChip(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    selected = selectedDuration == seconds,
                    onClick = { onDurationChange(seconds) },
                    label = { 
                        Text(if (seconds == 0) stringResource(R.string.quiz_duration_unlimited) else "$seconds sn") 
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.btn_start_quiz), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun GameOverScreen(score: Int, correct: Int, wrong: Int, total: Int, onRestart: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎉",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    stringResource(R.string.game_over_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.quiz_score_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(score.toString(), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(correct.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Text(stringResource(R.string.stat_correct), style = MaterialTheme.typography.labelMedium, color = Color(0xFF10B981))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(wrong.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(stringResource(R.string.stat_wrong), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.quiz_answered_questions, total), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.btn_try_again), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
