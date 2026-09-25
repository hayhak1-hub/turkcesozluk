package com.hayhak.turkcesozluk.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Popup
import androidx.core.os.ConfigurationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.ui.components.DailyWordCard
import com.hayhak.turkcesozluk.ui.components.ResultCard
import com.hayhak.turkcesozluk.ui.components.SearchBar
import com.hayhak.turkcesozluk.ui.components.TdkResultCard
import com.hayhak.turkcesozluk.util.capitalizeTR
import com.hayhak.turkcesozluk.util.normalizeTR
import com.hayhak.turkcesozluk.util.pressScale
import com.hayhak.turkcesozluk.data.db.DictionaryMode
import com.hayhak.turkcesozluk.viewmodel.DictionaryViewModel
import java.util.Locale

/** Sözlük kipinin kullanıcıya gösterilen adı; Oyun ekranı da bunu kullanır. */
@Composable
internal fun dictionaryModeTitle(mode: DictionaryMode): String = when (mode) {
    DictionaryMode.SYNONYMS -> stringResource(R.string.dictionary_title_synonyms)
    DictionaryMode.VERBS -> stringResource(R.string.dictionary_title_verbs)
    DictionaryMode.DEFINITIONS -> stringResource(R.string.mode_definitions)
    DictionaryMode.IDIOMS -> stringResource(R.string.mode_idioms)
    DictionaryMode.ADJECTIVES -> stringResource(R.string.mode_adjectives)
    DictionaryMode.ALL -> stringResource(R.string.dictionary_title_all)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val searchUiState by viewModel.searchUiState.collectAsState()
    val query = searchUiState.query
    val results = searchUiState.results
    val wordTree = searchUiState.wordTree
    val suggestions = searchUiState.suggestions
    val tdkResult = searchUiState.tdkResult
    val tdkLoading = searchUiState.tdkLoading
    val tdkError = searchUiState.tdkError
    val recentSearches by viewModel.recentSearches.collectAsState()
    val favoriteWordsSet by viewModel.favoriteWordsSet.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val showDailyWord by viewModel.showDailyWord.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val hasSeenModeHint by viewModel.hasSeenModeHint.collectAsState()
    val configuration = LocalConfiguration.current
    val displayLocale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    }
    val dictionaryTitle = dictionaryModeTitle(currentMode)
    val trLocale = remember { Locale("tr", "TR") }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Bildirim izni yalnizca kullanici gunun kelimesi ozelligini actiginda,
    // yani izin gercekten bir ise yarayacakken istenir.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

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

    val fabInteractionSource = remember { MutableInteractionSource() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.pressScale(fabInteractionSource),
                interactionSource = fabInteractionSource,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_word))
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
                .padding(padding),
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
                        text = dictionaryTitle.uppercase(displayLocale),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
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
                                IconButton(onClick = {
                                    showSettingsMenu = true
                                    if (!hasSeenModeHint) viewModel.dismissModeHint()
                                }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.cd_settings),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (!hasSeenModeHint) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color(0xFFEF4444), CircleShape)
                                    )
                                    Popup(
                                        alignment = Alignment.TopEnd,
                                        offset = with(LocalDensity.current) {
                                            IntOffset(0, 48.dp.roundToPx())
                                        },
                                        onDismissRequest = { viewModel.dismissModeHint() }
                                    ) {
                                        Card(
                                            modifier = Modifier.width(220.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    stringResource(R.string.mode_hint_title),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    stringResource(R.string.mode_hint_body),
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                TextButton(
                                                    onClick = { viewModel.dismissModeHint() },
                                                    modifier = Modifier.align(Alignment.End),
                                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                                ) {
                                                    Text(stringResource(R.string.mode_hint_ok), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
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
                                            com.hayhak.turkcesozluk.data.db.SettingsManager.setDictionaryMode(context, com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS)
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = { 
                                            RadioButton(selected = currentMode == com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS, onClick = null) 
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.mode_verbs)) },
                                        onClick = {
                                            com.hayhak.turkcesozluk.data.db.SettingsManager.setDictionaryMode(context, com.hayhak.turkcesozluk.data.db.DictionaryMode.VERBS)
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = {
                                            RadioButton(selected = currentMode == com.hayhak.turkcesozluk.data.db.DictionaryMode.VERBS, onClick = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.mode_definitions)) },
                                        onClick = {
                                            com.hayhak.turkcesozluk.data.db.SettingsManager.setDictionaryMode(context, com.hayhak.turkcesozluk.data.db.DictionaryMode.DEFINITIONS)
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = {
                                            RadioButton(selected = currentMode == com.hayhak.turkcesozluk.data.db.DictionaryMode.DEFINITIONS, onClick = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.mode_idioms)) },
                                        onClick = {
                                            com.hayhak.turkcesozluk.data.db.SettingsManager.setDictionaryMode(context, com.hayhak.turkcesozluk.data.db.DictionaryMode.IDIOMS)
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = {
                                            RadioButton(selected = currentMode == com.hayhak.turkcesozluk.data.db.DictionaryMode.IDIOMS, onClick = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.mode_adjectives)) },
                                        onClick = {
                                            com.hayhak.turkcesozluk.data.db.SettingsManager.setDictionaryMode(context, com.hayhak.turkcesozluk.data.db.DictionaryMode.ADJECTIVES)
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = {
                                            RadioButton(selected = currentMode == com.hayhak.turkcesozluk.data.db.DictionaryMode.ADJECTIVES, onClick = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.mode_all)) },
                                        onClick = {
                                            com.hayhak.turkcesozluk.data.db.SettingsManager.setDictionaryMode(context, com.hayhak.turkcesozluk.data.db.DictionaryMode.ALL)
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = {
                                            RadioButton(selected = currentMode == com.hayhak.turkcesozluk.data.db.DictionaryMode.ALL, onClick = null)
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_show_daily_word)) },
                                        onClick = {
                                            val enabling = !showDailyWord
                                            com.hayhak.turkcesozluk.data.db.SettingsManager.setShowDailyWord(context, enabling)
                                            if (enabling && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                            showSettingsMenu = false
                                        },
                                        leadingIcon = {
                                            Checkbox(checked = showDailyWord, onCheckedChange = null)
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
                AnimatedVisibility(
                    visible = showDailyWord,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val modeLabel = stringResource(
                        when(currentMode) {
                            com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS -> R.string.label_synonym_text
                            com.hayhak.turkcesozluk.data.db.DictionaryMode.VERBS -> R.string.label_meaning_text
                            com.hayhak.turkcesozluk.data.db.DictionaryMode.DEFINITIONS -> R.string.label_meaning_text
                            com.hayhak.turkcesozluk.data.db.DictionaryMode.IDIOMS -> R.string.label_meaning_text
                            com.hayhak.turkcesozluk.data.db.DictionaryMode.ADJECTIVES -> R.string.label_meaning_text
                            com.hayhak.turkcesozluk.data.db.DictionaryMode.ALL -> R.string.label_meaning_text
                        }
                    )
                    DailyWordCard(
                        word = dailyWord.first,
                        synonym = dailyWord.second,
                        isFavorite = isDailyFavorite,
                        onToggleFavorite = { viewModel.toggleFavorite(dailyWord.first, dailyWord.second, isDailyFavorite) },
                        onSpeak = { tts?.speak("${dailyWord.first} $modeLabel ${dailyWord.second}", TextToSpeech.QUEUE_FLUSH, null, null) },
                        onClose = { com.hayhak.turkcesozluk.data.db.SettingsManager.setShowDailyWord(context, false) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                SearchBar(
                    query = query,
                    onQueryChange = { viewModel.updateQuery(it) },
                    voiceLauncher = voiceLauncher
                )

                if (query.isEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.tdk_discovery_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

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

                val luckyInteractionSource = remember { MutableInteractionSource() }

                Button(
                    onClick = { viewModel.getRandomWord()?.let { viewModel.updateQuery(it.first) } },
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth().height(56.dp)
                        .pressScale(luckyInteractionSource)
                        .scale(if (query.isEmpty()) scale else 1f),
                    interactionSource = luckyInteractionSource,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.btn_try_luck),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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

                val searchContentKey = when {
                    results.isNotEmpty() -> "result"
                    tdkLoading -> "loading"
                    tdkResult != null -> "tdk"
                    tdkError != null -> "error"
                    query.length > 1 && suggestions.isEmpty() -> "notfound"
                    else -> "empty"
                }

                AnimatedVisibility(visible = query.isNotEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    AnimatedContent(
                        targetState = searchContentKey,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 8 }) togetherWith
                                fadeOut(tween(120))
                        },
                        label = "searchResultContent"
                    ) { key ->
                    if (key == "result") {
                        val isResultFavorite = favoriteWordsSet.contains(query.normalizeTR())
                        ResultCard(
                            query = query,
                            results = results,
                            wordTree = wordTree,
                            isFavorite = isResultFavorite,
                            onToggleFavorite = { viewModel.toggleFavorite(query, results.joinToString(", "), isResultFavorite) },
                            onSpeak = { tts?.speak(results.joinToString(", "), TextToSpeech.QUEUE_FLUSH, null, null) },
                            onShare = {
                                val modeName = when (currentMode) {
                                    com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS -> context.getString(R.string.label_synonym_text).lowercase(trLocale)
                                    com.hayhak.turkcesozluk.data.db.DictionaryMode.VERBS -> context.getString(R.string.share_mode_verbs)
                                    com.hayhak.turkcesozluk.data.db.DictionaryMode.DEFINITIONS -> context.getString(R.string.share_mode_definitions)
                                    com.hayhak.turkcesozluk.data.db.DictionaryMode.IDIOMS -> context.getString(R.string.share_mode_idioms)
                                    com.hayhak.turkcesozluk.data.db.DictionaryMode.ADJECTIVES -> context.getString(R.string.share_mode_adjectives)
                                    com.hayhak.turkcesozluk.data.db.DictionaryMode.ALL -> context.getString(R.string.share_mode_all)
                                }
                                val shareText = context.getString(
                                    R.string.share_result_template,
                                    query.uppercase(trLocale),
                                    modeName,
                                    results.joinToString(", ")
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_word_title)))
                            },
                            onWordClick = { viewModel.updateQuery(it) },
                            mode = currentMode
                        )
                    } else if (key == "loading") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.tdk_loading),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (key == "tdk" && tdkResult != null) {
                        val entry = tdkResult
                        Column {
                            Text(
                                text = stringResource(R.string.tdk_fallback_hint),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TdkResultCard(
                                entry = entry,
                                onSpeak = {
                                    val speakText = buildString {
                                        append(entry.word)
                                        entry.meanings.firstOrNull()?.let {
                                            append(". ")
                                            append(it.meaning)
                                        }
                                    }
                                    tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, null)
                                },
                                onWordClick = { viewModel.updateQuery(it) }
                            )
                        }
                    } else if (key == "error" && tdkError != null) {
                        val message = when (tdkError) {
                            com.hayhak.turkcesozluk.data.model.TdkErrorReason.SERVER ->
                                stringResource(R.string.tdk_error_server)
                            com.hayhak.turkcesozluk.data.model.TdkErrorReason.NETWORK ->
                                stringResource(R.string.tdk_error_network)
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else if (key == "notfound") {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
                            Text(stringResource(R.string.no_result_found), modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
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
