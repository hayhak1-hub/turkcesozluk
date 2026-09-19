package com.hayhak.turkcesozluk.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.ui.components.DefinitionCard
import com.hayhak.turkcesozluk.ui.components.SearchBar
import com.hayhak.turkcesozluk.util.capitalizeTR
import com.hayhak.turkcesozluk.viewmodel.DefinitionViewModel
import java.util.Locale

@Composable
fun DefinitionScreen(viewModel: DefinitionViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val trLocale = remember { Locale("tr", "TR") }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) instance?.language = trLocale
        }
        tts = instance
        onDispose { instance?.stop(); instance?.shutdown() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.tab_definition).uppercase(trLocale),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.preparing_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.definition_word_count, viewModel.wordCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        item {
            SearchBar(
                query = uiState.query,
                onQueryChange = { viewModel.updateQuery(it) }
            )

            if (uiState.suggestions.isNotEmpty() && uiState.result == null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column {
                        uiState.suggestions.forEach { suggestion ->
                            ListItem(
                                headlineContent = {
                                    Text(suggestion.capitalizeTR(), fontWeight = FontWeight.Medium)
                                },
                                modifier = Modifier.clickable { viewModel.updateQuery(suggestion) },
                                leadingContent = {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.getRandomWord().takeIf { it.isNotEmpty() }?.let(viewModel::updateQuery) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.definition_random_word),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        item {
            AnimatedVisibility(
                visible = uiState.query.isNotEmpty(),
                enter = expandVertically(tween(300)) + fadeIn(),
                exit = shrinkVertically(tween(200)) + fadeOut()
            ) {
                when {
                    uiState.result != null -> {
                        DefinitionCard(
                            definition = uiState.result!!,
                            onSpeak = {
                                tts?.speak(uiState.result!!.word, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        )
                    }
                    uiState.suggestions.isEmpty() && uiState.query.length > 1 -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                stringResource(R.string.no_result_found),
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
