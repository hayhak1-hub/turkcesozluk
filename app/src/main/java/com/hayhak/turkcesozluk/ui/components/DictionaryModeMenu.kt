package com.hayhak.turkcesozluk.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.data.db.DictionaryMode

private val ModeLabels = listOf(
    DictionaryMode.SYNONYMS to R.string.mode_synonyms,
    DictionaryMode.VERBS to R.string.mode_verbs,
    DictionaryMode.DEFINITIONS to R.string.mode_definitions,
    DictionaryMode.IDIOMS to R.string.mode_idioms,
    DictionaryMode.ADJECTIVES to R.string.mode_adjectives,
    DictionaryMode.ALL to R.string.mode_all,
)

/** Sözlük ekranındaki ayarlar menüsü: dişli ikon + sözlük kipi listesi. */
@Composable
fun DictionaryModeMenuButton(
    currentMode: DictionaryMode,
    onModeSelected: (DictionaryMode) -> Unit,
    modifier: Modifier = Modifier,
    onMenuOpened: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = {
            expanded = true
            onMenuOpened()
        }) {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DictionaryModeMenuItems(
                currentMode = currentMode,
                onSelect = { mode ->
                    onModeSelected(mode)
                    expanded = false
                },
            )
        }
    }
}

@Composable
fun DictionaryModeMenuItems(
    currentMode: DictionaryMode,
    onSelect: (DictionaryMode) -> Unit,
) {
    Text(
        text = stringResource(R.string.settings_dictionary_mode),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    ModeLabels.forEach { (mode, labelRes) ->
        DropdownMenuItem(
            text = { Text(stringResource(labelRes)) },
            onClick = { onSelect(mode) },
            leadingIcon = {
                RadioButton(selected = mode == currentMode, onClick = null)
            },
        )
    }
}
