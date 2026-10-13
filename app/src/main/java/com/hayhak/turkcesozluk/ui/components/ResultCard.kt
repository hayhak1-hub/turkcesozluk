package com.hayhak.turkcesozluk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.util.TurkishUtils

@Composable
fun ResultCard(
    query: String,
    results: List<String>,
    wordTree: Map<String, List<String>>,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSpeak: () -> Unit,
    onShare: () -> Unit,
    onWordClick: (String) -> Unit,
    mode: com.hayhak.turkcesozluk.data.db.DictionaryMode = com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS
) {
    val label = when (mode) {
        com.hayhak.turkcesozluk.data.db.DictionaryMode.SYNONYMS -> {
            if (results.size == 1) stringResource(R.string.label_synonym_text)
            else stringResource(R.string.label_synonyms_multiple_text, results.size)
        }
        com.hayhak.turkcesozluk.data.db.DictionaryMode.VERBS -> {
            if (results.size == 1) stringResource(R.string.label_meaning_text)
            else stringResource(R.string.label_meanings_multiple_text, results.size)
        }
        com.hayhak.turkcesozluk.data.db.DictionaryMode.DEFINITIONS -> {
            if (results.size == 1) stringResource(R.string.label_definition_text)
            else stringResource(R.string.label_meanings_multiple_text, results.size)
        }
        com.hayhak.turkcesozluk.data.db.DictionaryMode.IDIOMS -> {
            if (results.size == 1) stringResource(R.string.label_meaning_text)
            else stringResource(R.string.label_meanings_multiple_text, results.size)
        }
        com.hayhak.turkcesozluk.data.db.DictionaryMode.ADJECTIVES -> {
            if (results.size == 1) stringResource(R.string.label_meaning_text)
            else stringResource(R.string.label_meanings_multiple_text, results.size)
        }
        com.hayhak.turkcesozluk.data.db.DictionaryMode.ALL -> {
            if (results.size == 1) stringResource(R.string.label_meaning_text)
            else stringResource(R.string.label_meanings_multiple_text, results.size)
        }
    }

    Card(
        border = com.hayhak.turkcesozluk.ui.theme.appCardBorder(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        // Canlı mor-mavi gradyan zemin (WifiGuard paleti); üstündeki metinler beyaz.
        val gradient = Brush.linearGradient(
            if (MaterialTheme.colorScheme.background.luminance() < 0.5f) listOf(Color(0xFF4527A0), Color(0xFF1565C0))
            else listOf(Color(0xFF6D44E0), Color(0xFF3B82F6))
        )
        val onGradient = Color.White
        val tonalButtons = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = Color.White.copy(alpha = 0.22f),
            contentColor = onGradient
        )
        Column(modifier = Modifier.background(gradient).padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label.uppercase(TurkishUtils.trLocale),
                style = MaterialTheme.typography.labelLarge, 
                color = onGradient.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            results.forEachIndexed { index, synonym ->
                val isClickable = synonym.split(" ").size <= 3
                Text(
                    TurkishUtils.capitalize(synonym),
                    style = if (index == 0) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineSmall, 
                    color = onGradient.copy(alpha = if (index == 0) 1f else 0.85f), 
                    fontWeight = if (index == 0) FontWeight.ExtraBold else FontWeight.SemiBold, 
                    textAlign = TextAlign.Center,
                    modifier = if (isClickable) Modifier.clickable { onWordClick(synonym) } else Modifier
                )
                if (index < results.size - 1) Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(16.dp))
            Row {
                FilledTonalIconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(56.dp),
                    colors = tonalButtons
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        contentDescription = stringResource(R.string.cd_favorite), 
                        tint = if (isFavorite) Color(0xFFFF8FA3) else onGradient
                    )
                }
                Spacer(Modifier.width(12.dp))
                FilledIconButton(
                    onClick = onSpeak,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = Color(0xFF4527A0))
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.cd_listen), modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(12.dp))
                FilledTonalIconButton(onClick = onShare, modifier = Modifier.size(56.dp), colors = tonalButtons) {
                    Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.cd_share))
                }
            }

            if (wordTree.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Text(stringResource(R.string.word_tree_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = onGradient)
                Spacer(Modifier.height(16.dp))
                
                @OptIn(ExperimentalLayoutApi::class)
                wordTree.forEach { (parent, children) ->
                    Card(
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val isParentClickable = parent.split(" ").size <= 3
                            Text(
                                TurkishUtils.capitalize(parent), 
                                fontWeight = FontWeight.Bold,
                                color = onGradient,
                                modifier = if (isParentClickable) Modifier.clickable { onWordClick(parent) } else Modifier
                            )
                            Spacer(Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward, 
                                    contentDescription = null, 
                                    tint = onGradient,
                                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically)
                                )
                                children.forEach { child ->
                                    val isChildClickable = child.split(" ").size <= 3
                                    Surface(
                                        onClick = { if (isChildClickable) onWordClick(child) },
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(4.dp),
                                        enabled = isChildClickable
                                    ) {
                                        Text(
                                            TurkishUtils.capitalize(child),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = onGradient.copy(alpha = 0.92f),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
