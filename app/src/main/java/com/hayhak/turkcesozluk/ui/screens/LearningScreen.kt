package com.hayhak.turkcesozluk.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.viewmodel.LearningViewModel

@Composable
fun LearningScreen(viewModel: LearningViewModel, onDismiss: () -> Unit) {
    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (!viewModel.isLearningActive && !viewModel.isLoading) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.learning_no_favorites_title)) },
            text = { Text(stringResource(R.string.learning_no_favorites_message)) },
            confirmButton = {
                Button(onClick = onDismiss) { Text(stringResource(R.string.btn_ok)) }
            }
        )
    }

    val currentCard = viewModel.getCurrentCard()
    
    // Kart çevirme animasyonu
    val rotation by animateFloatAsState(
        targetValue = if (viewModel.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "CardFlip"
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_close))
            }
            Text(stringResource(R.string.learning_cards_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Box(Modifier.size(48.dp)) // Denge için boşluk
        }

        LinearProgressIndicator(
            progress = { viewModel.getProgress() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(8.dp),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(Modifier.height(16.dp))

        if (currentCard != null) {
            // Flashcard
            Card(
                onClick = { viewModel.flipCard() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (rotation > 90f) MaterialTheme.colorScheme.primaryContainer 
                                     else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (rotation <= 90f) {
                        // Ön yüz (Kelime)
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.learning_word_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text(currentCard.word, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        }
                    } else {
                        // Arka yüz (Eş Anlamlı)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer { rotationY = 180f }.verticalScroll(rememberScrollState()).padding(16.dp)
                        ) {
                            Text(stringResource(R.string.learning_synonym_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.height(16.dp))
                            Text(currentCard.synonym, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.rateCard(false) },
            enabled = viewModel.isFlipped && !viewModel.saving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.study_again)) }

        Button(
            onClick = { viewModel.rateCard(true) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(20.dp),
            enabled = viewModel.isFlipped && !viewModel.saving
        ) {
            Text(
                stringResource(R.string.study_known),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, Modifier.padding(start = 8.dp))
        }
        
        Text(
            stringResource(R.string.learning_flip_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
