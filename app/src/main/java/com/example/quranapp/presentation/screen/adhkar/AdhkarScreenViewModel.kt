package com.example.quranapp.presentation.screen.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.model.AdhkarCategory
import com.example.quranapp.data.model.AdhkarCategoryItem
import com.example.quranapp.data.model.Dhikr
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
    val dhikrProgress: Map<Int, Int> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class AdhkarScreenViewModel @Inject constructor(
    private val repository: AdhkarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdhkarUiState())
    val uiState: StateFlow<AdhkarUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
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
                _uiState.update {
                    it.copy(
                        selectedCategory = category,
                        dhikrProgress = category?.content?.indices?.associateWith { 0 } ?: emptyMap(),
                        isLoading = false
                    )
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
        _uiState.update { state ->
            val currentProgress = state.dhikrProgress[index] ?: 0
            val newProgress = if (currentProgress < maxRepeat) currentProgress + 1 else maxRepeat
            state.copy(
                dhikrProgress = state.dhikrProgress + (index to newProgress)
            )
        }
    }

    fun resetDhikrProgress(index: Int) {
        _uiState.update { state ->
            state.copy(
                dhikrProgress = state.dhikrProgress + (index to 0)
            )
        }
    }

    fun resetAllProgress() {
        _uiState.update { state ->
            state.copy(
                dhikrProgress = state.selectedCategory?.content?.indices?.associateWith { 0 } ?: emptyMap()
            )
        }
    }

    fun clearSelectedCategory() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                dhikrProgress = emptyMap()
            )
        }
    }
}
