package com.hayhak.esanlamli.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.esanlamli.R
import com.hayhak.esanlamli.ui.components.DailyWordCard
import com.hayhak.esanlamli.ui.components.ResultCard
import com.hayhak.esanlamli.ui.components.SearchBar
import com.hayhak.esanlamli.util.capitalizeTR
import com.hayhak.esanlamli.util.normalizeTR
import com.hayhak.esanlamli.viewmodel.DictionaryViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel = hiltViewModel(),
    onNavigateToLearning: () -> Unit = {}
) {
    val context = LocalContext.current
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val wordTree by viewModel.wordTree.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val favoriteWordsSet by viewModel.favoriteWordsSet.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val dictionaryTitle by viewModel.dictionaryTitle.collectAsState()
    val trLocale = remember { Locale("tr", "TR") }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!data.isNullOrEmpty()) {
            viewModel.updateQuery(data[0])
        }
    }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = trLocale
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    val dailyWord by viewModel.dailyWord.collectAsState()
    val isDailyFavorite = favoriteWordsSet.contains(dailyWord.first.normalizeTR())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Kelime Ekle")
            }
        }
    ) { padding ->
        if (showAddDialog) {
            AddWordDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { word, synonym ->
                    viewModel.addNewWord(word, synonym)
                    showAddDialog = false
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(), // Status bar ile çakışmayı önler
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top // Butonların yukarıda hizalanmasını sağlar
                ) {
                    Text(
                        text = dictionaryTitle.uppercase(trLocale), 
                        style = MaterialTheme.typography.headlineMedium, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔥", fontSize = 16.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = streakCount.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            
                            Spacer(Modifier.width(8.dp))

                            Box {
                                IconButton(onClick = { showSettingsMenu = true }) {
                                    Icon(
                                        Icons.Default.Settings, 
                                        contentDescription = "Ayarlar",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSettingsMenu,
                                    onDismissRequest = { showSettingsMenu = false }
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_dictionary_mode),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.mode_synonyms)) },
                                        onClick = {
                                            com.hayhak.esanlamli.data.db.SettingsManager.setDictionaryMode(context, com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS)
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = { 
                                            RadioButton(selected = currentMode == com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS, onClick = null) 
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.mode_verbs)) },
                                        onClick = {
                                            com.hayhak.esanlamli.data.db.SettingsManager.setDictionaryMode(context, com.hayhak.esanlamli.data.db.DictionaryMode.VERBS)
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = { 
                                            RadioButton(selected = currentMode == com.hayhak.esanlamli.data.db.DictionaryMode.VERBS, onClick = null) 
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.word_count_loaded, viewModel.synonymsCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                val modeLabel = stringResource(if (currentMode == com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS) R.string.label_synonym_text else R.string.label_meaning_text)
                DailyWordCard(
                    word = dailyWord.first,
                    synonym = dailyWord.second,
                    isFavorite = isDailyFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite(dailyWord.first, dailyWord.second, isDailyFavorite) },
                    onSpeak = { tts?.speak("${dailyWord.first} $modeLabel ${dailyWord.second}", TextToSpeech.QUEUE_FLUSH, null, null) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                SearchBar(
                    query = query,
                    onQueryChange = { viewModel.updateQuery(it) },
                    voiceLauncher = voiceLauncher
                )

                if (suggestions.isNotEmpty() && results.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column {
                            suggestions.forEach { suggestion ->
                                ListItem(
                                    headlineContent = { Text(suggestion.capitalizeTR(), fontWeight = FontWeight.Medium) },
                                    modifier = Modifier.clickable { viewModel.updateQuery(suggestion) },
                                    leadingContent = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    }
                }

                val infiniteTransition = rememberInfiniteTransition(label = "")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse), label = ""
                )

                Row(
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.updateQuery(viewModel.getRandomWord().first) },
                        modifier = Modifier.weight(1f).height(56.dp).scale(if (query.isEmpty()) scale else 1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_try_luck), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    FilledTonalButton(
                        onClick = onNavigateToLearning,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.tab_learning).uppercase(trLocale), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                if (recentSearches.isNotEmpty() && query.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.recent_searches), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.fillMaxWidth())
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentSearches.forEach { history ->
                            SuggestionChip(
                                onClick = { viewModel.updateQuery(history.word) }, 
                                label = { Text(history.word.capitalizeTR()) }, 
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(visible = query.isNotEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    if (results.isNotEmpty()) {
                        val isResultFavorite = favoriteWordsSet.contains(query.normalizeTR())
                        ResultCard(
                            query = query,
                            results = results,
                            wordTree = wordTree,
                            isFavorite = isResultFavorite,
                            onToggleFavorite = { viewModel.toggleFavorite(query, results.joinToString(", "), isResultFavorite) },
                            onSpeak = { tts?.speak(results.joinToString(", "), TextToSpeech.QUEUE_FLUSH, null, null) },
                            onShare = {
                                val modeName = if (currentMode == com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS) "eş anlamlısı" else "anlamı"
                                val shareText = "📖 *${query.uppercase(trLocale)}* kelimesinin $modeName: \n\n✨ ${results.joinToString(", ")}\n\n_Eş Anlamlı & Fiiller Sözlüğü ile öğreniyorum!_"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Kelimeyi Paylaş"))
                            },
                            onWordClick = { viewModel.updateQuery(it) },
                            mode = currentMode
                        )
                    } else if (query.length > 1 && suggestions.isEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
                            Text(stringResource(R.string.no_result_found), modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddWordDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var word by remember { mutableStateOf("") }
    var synonym by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_word), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text(stringResource(R.string.label_word)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = synonym,
                    onValueChange = { synonym = it },
                    label = { Text(stringResource(R.string.label_synonym)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (word.isNotBlank() && synonym.isNotBlank()) onConfirm(word, synonym) },
                enabled = word.isNotBlank() && synonym.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.btn_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
