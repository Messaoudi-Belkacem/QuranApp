package com.example.quranapp.presentation.screen.surah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.data.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewingMode {
    LIST,
    MUSHAF
}

data class SurahReadingUiState(
    val currentSurah: Surah? = null,
    val allAyahs: List<Ayah> = emptyList(),
    val filteredAyahs: List<Ayah> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val viewingMode: ViewingMode = ViewingMode.LIST,
    val selectedAyahId: Int? = null
)

@HiltViewModel
class SurahReadingViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurahReadingUiState())
    val uiState: StateFlow<SurahReadingUiState> = _uiState.asStateFlow()

    fun loadSurah(surahId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Load surah details and ayahs
                val surah = quranRepository.getSurahById(surahId)
                val ayahs = quranRepository.getAyahsBySurah(surahId)

                _uiState.value = _uiState.value.copy(
                    currentSurah = surah,
                    allAyahs = ayahs,
                    filteredAyahs = ayahs,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load Surah: ${exception.message}"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterAyahs(query)
    }

    private fun filterAyahs(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.allAyahs
        } else {
            _uiState.value.allAyahs.filter { ayah ->
                ayah.text.contains(query, ignoreCase = true) ||
                ayah.id.toString().contains(query)
            }
        }
        _uiState.value = _uiState.value.copy(filteredAyahs = filtered)
    }

    fun clearSearch() {
        updateSearchQuery("")
    }

    fun refreshSurah(surahId: Int) {
        loadSurah(surahId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun toggleViewingMode() {
        val newMode = if (_uiState.value.viewingMode == ViewingMode.LIST) {
            ViewingMode.MUSHAF
        } else {
            ViewingMode.LIST
        }
        _uiState.value = _uiState.value.copy(viewingMode = newMode)
    }

    fun selectAyah(ayahId: Int?) {
        _uiState.value = _uiState.value.copy(selectedAyahId = ayahId)
    }
}
