package com.hayhak.turkcesozluk.ui.components

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayhak.turkcesozluk.R

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    voiceLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            Row {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear))
                    }
                }
                if (voiceLauncher != null) {
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.voice_search_prompt))
                        }
                        voiceLauncher.launch(intent)
                    }) {
                        Icon(Icons.Rounded.Mic, contentDescription = stringResource(R.string.cd_voice_search), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    )
}
