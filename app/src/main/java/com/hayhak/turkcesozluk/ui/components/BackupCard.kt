package com.hayhak.turkcesozluk.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.viewmodel.BackupViewModel

@Composable
fun BackupCard(viewModel: BackupViewModel = hiltViewModel(), onRestored: () -> Unit = {}) {
    val latestOnRestored by rememberUpdatedState(onRestored)
    LaunchedEffect(viewModel.restoreGeneration) {
        if (viewModel.restoreGeneration > 0) latestOnRestored()
    }
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        if (it != null) viewModel.run(it, false)
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        pending = it?.toString()
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.backup_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.backup_description), style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { export.launch("turkcesozluk-backup.json") }, enabled = !viewModel.busy,
                modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.backup_export)) }
            OutlinedButton(onClick = { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                enabled = !viewModel.busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.backup_restore)) }
            if (viewModel.busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            viewModel.result?.let { Text(stringResource(if (it) R.string.backup_success else R.string.backup_error)) }
        }
    }
    pending?.let { value ->
        AlertDialog(onDismissRequest = { pending = null },
            title = { Text(stringResource(R.string.backup_restore)) },
            text = { Text(stringResource(R.string.backup_confirm)) },
            confirmButton = { TextButton(onClick = { pending = null; viewModel.run(Uri.parse(value), true) }) {
                Text(stringResource(R.string.btn_ok))
            } },
            dismissButton = { TextButton(onClick = { pending = null }) { Text(stringResource(R.string.btn_cancel)) } })
    }
}
