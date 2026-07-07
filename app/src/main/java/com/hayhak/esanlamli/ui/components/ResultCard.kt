package com.hayhak.esanlamli.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hayhak.esanlamli.R
import com.hayhak.esanlamli.util.TurkishUtils

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
    mode: com.hayhak.esanlamli.data.db.DictionaryMode = com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS
) {
    val label = if (mode == com.hayhak.esanlamli.data.db.DictionaryMode.SYNONYMS) {
        if (results.size == 1) stringResource(R.string.label_synonym_text) 
        else stringResource(R.string.label_synonyms_multiple_text, results.size)
    } else {
        if (results.size == 1) stringResource(R.string.label_meaning_text) 
        else stringResource(R.string.label_meanings_multiple_text, results.size)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label.uppercase(TurkishUtils.trLocale),
                style = MaterialTheme.typography.labelLarge, 
                color = MaterialTheme.colorScheme.secondary, 
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            results.forEachIndexed { index, synonym ->
                val isClickable = synonym.split(" ").size <= 3
                Text(
                    TurkishUtils.capitalize(synonym),
                    style = if (index == 0) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineSmall, 
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (index == 0) 1f else 0.75f), 
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
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        contentDescription = "Favori", 
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
                FilledIconButton(onClick = onSpeak, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Dinle", modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(12.dp))
                FilledTonalIconButton(onClick = onShare, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.Share, contentDescription = "Paylaş")
                }
            }

            if (wordTree.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Text("KELİME AĞACI", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                
                @OptIn(ExperimentalLayoutApi::class)
                wordTree.forEach { (parent, children) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val isParentClickable = parent.split(" ").size <= 3
                            Text(
                                TurkishUtils.capitalize(parent), 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary,
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
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
