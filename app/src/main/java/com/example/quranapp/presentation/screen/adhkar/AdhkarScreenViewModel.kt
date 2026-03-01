package com.example.quranapp.presentation.screen.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.model.AdhkarCategory
import com.example.quranapp.data.model.AdhkarCategoryItem
import com.example.quranapp.data.repository.AdhkarPreferencesRepository
import com.example.quranapp.data.repository.AdhkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdhkarUiState(
    val isLoading: Boolean = true,
    val categories: List<AdhkarCategoryItem> = emptyList(),
    val selectedCategory: AdhkarCategory? = null,
    val selectedCategoryFileName: String? = null,
    val dhikrProgress: Map<Int, Int> = emptyMap(),
    val totalScore: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class AdhkarScreenViewModel @Inject constructor(
    private val repository: AdhkarRepository,
    private val preferencesRepository: AdhkarPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdhkarUiState())
    val uiState: StateFlow<AdhkarUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadTotalScore()
    }

    private fun loadTotalScore() {
        viewModelScope.launch {
            preferencesRepository.getTotalScore().collect { score ->
                _uiState.update { it.copy(totalScore = score) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val categories = repository.getCategories()
                _uiState.update {
                    it.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load categories: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadCategoryDetails(fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val category = repository.getCategoryAdhkar(fileName)
                if (category != null) {
                    // Load saved progress from DataStore
                    val savedProgress = preferencesRepository.getCategoryProgress(
                        fileName,
                        category.content.size
                    )
                    _uiState.update {
                        it.copy(
                            selectedCategory = category,
                            selectedCategoryFileName = fileName,
                            dhikrProgress = savedProgress,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Failed to load adhkar: category not found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load adhkar: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun incrementDhikrProgress(index: Int, maxRepeat: Int) {
        viewModelScope.launch {
            val currentProgress = _uiState.value.dhikrProgress[index] ?: 0
            if (currentProgress < maxRepeat) {
                val newProgress = currentProgress + 1

                // Update UI state
                _uiState.update { state ->
                    state.copy(
                        dhikrProgress = state.dhikrProgress + (index to newProgress)
                    )
                }

                // Save progress to DataStore
                val categoryFileName = _uiState.value.selectedCategoryFileName
                if (categoryFileName != null) {
                    preferencesRepository.setDhikrProgress(categoryFileName, index, newProgress)

                    // If dhikr is completed, add score
                    if (newProgress == maxRepeat) {
                        preferencesRepository.addScore(maxRepeat)

                        // Check if all adhkar in category are completed
                        checkCategoryCompletion()
                    }
                }
            }
        }
    }

    private fun checkCategoryCompletion() {
        viewModelScope.launch {
            val state = _uiState.value
            val category = state.selectedCategory ?: return@launch

            val allCompleted = category.content.indices.all { index ->
                val progress = state.dhikrProgress[index] ?: 0
                val maxRepeat = category.content[index].repeat
                progress >= maxRepeat
            }

            if (allCompleted) {
                val categoryFileName = state.selectedCategoryFileName
                if (categoryFileName != null) {
                    preferencesRepository.markCategoryCompleted(categoryFileName)
                }
            }
        }
    }

    fun resetDhikrProgress(index: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    dhikrProgress = state.dhikrProgress + (index to 0)
                )
            }

            // Save to DataStore
            val categoryFileName = _uiState.value.selectedCategoryFileName
            if (categoryFileName != null) {
                preferencesRepository.setDhikrProgress(categoryFileName, index, 0)
            }
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            val category = _uiState.value.selectedCategory
            val categoryFileName = _uiState.value.selectedCategoryFileName

            if (category != null && categoryFileName != null) {
                // Reset in UI
                _uiState.update { state ->
                    state.copy(
                        dhikrProgress = category.content.indices.associateWith { 0 }
                    )
                }

                // Reset in DataStore
                preferencesRepository.resetCategoryProgress(categoryFileName, category.content.size)
                preferencesRepository.clearCategoryCompletion(categoryFileName)
            }
        }
    }

    fun clearSelectedCategory() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                selectedCategoryFileName = null,
                dhikrProgress = emptyMap()
            )
        }
    }
}
