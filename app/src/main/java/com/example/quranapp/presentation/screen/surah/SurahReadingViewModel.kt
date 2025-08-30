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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SurahReadingUiState(
    val currentSurah: Surah? = null,
    val ayahs: List<Ayah> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
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
                // Load surah details
                quranRepository.getSurahById(surahId)
                    .catch { exception: Throwable ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load Surah: ${exception.message}"
                        )
                        return@catch
                    }
                    .collect { surah: Surah ->
                        _uiState.value = _uiState.value.copy(currentSurah = surah)
                    }

                // Load ayahs for this surah
                quranRepository.getAyahsBySurah(surahId)
                    .catch { exception: Throwable ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load Ayahs: ${exception.message}"
                        )
                        return@catch
                    }
                    .collect { ayahs: List<Ayah> ->
                        _uiState.value = _uiState.value.copy(
                            ayahs = ayahs,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred: ${exception.message}"
                )
            }
        }
    }

    fun refreshSurah(surahId: Int) {
        loadSurah(surahId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
