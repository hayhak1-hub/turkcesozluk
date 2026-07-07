package com.hayhak.esanlamli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hayhak.esanlamli.data.model.WordDefinition
import com.hayhak.esanlamli.data.repository.CoreRepository
import com.hayhak.esanlamli.data.repository.DefinitionRepository
import com.hayhak.esanlamli.util.normalizeTR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DefinitionUiState(
    val query: String = "",
    val result: WordDefinition? = null,
    val suggestions: List<String> = emptyList()
)

@HiltViewModel
class DefinitionViewModel @Inject constructor(
    private val coreRepository: CoreRepository,
    private val definitionRepository: DefinitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DefinitionUiState())
    val uiState = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(definitionRepository.wordCount == 0)
    val isLoading = _isLoading.asStateFlow()

    val wordCount: Int get() = definitionRepository.wordCount

    private var searchJob: Job? = null

    init {
        if (_isLoading.value) {
            viewModelScope.launch {
                coreRepository.ensureDefinitionsLoaded()
                _isLoading.value = false
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.Default) {
            val normalized = newQuery.normalizeTR()
            val result = if (normalized.isNotBlank()) definitionRepository.search(normalized) else null
            val suggestions = if (result == null && normalized.isNotBlank()) {
                definitionRepository.getSuggestions(normalized)
            } else emptyList()
            _uiState.update { it.copy(result = result, suggestions = suggestions) }
        }
    }

    fun getRandomWord(): String = definitionRepository.getRandomWord()
}
