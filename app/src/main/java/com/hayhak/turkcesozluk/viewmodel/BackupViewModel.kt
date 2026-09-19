package com.hayhak.turkcesozluk.viewmodel

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.turkcesozluk.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(private val repository: BackupRepository) : ViewModel() {
    var busy by mutableStateOf(false)
        private set
    var result by mutableStateOf<Boolean?>(null)
    var restoreGeneration by mutableIntStateOf(0)
        private set
    fun run(uri: Uri, restore: Boolean) {
        if (busy) return
        busy = true
        result = null
        viewModelScope.launch {
            try {
                if (restore) repository.restore(uri) else repository.export(uri)
                if (restore) restoreGeneration++
                result = true
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) { result = false
            } finally { busy = false }
        }
    }
}
