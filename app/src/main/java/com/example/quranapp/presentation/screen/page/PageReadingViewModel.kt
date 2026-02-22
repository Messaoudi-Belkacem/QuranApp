package com.example.quranapp.presentation.screen.page

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

/**
 * Represents a group of ayahs belonging to the same surah within a page.
 */
data class SurahAyahGroup(
    val surah: Surah,
    val ayahs: List<Ayah>
)

/**
 * UI State for the Page Reading Screen.
 */
data class PageReadingUiState(
    val pageNumber: Int = 1,
    val surahGroups: List<SurahAyahGroup> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedAyahId: Int? = null,
    val selectedAyahSurahId: Int? = null,
    val totalPages: Int = 604
)

/**
 * ViewModel for managing Page Reading Screen state and business logic.
 * Loads ayahs for a specific Quran page number, grouped by their surah.
 */
@HiltViewModel
class PageReadingViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PageReadingUiState())
    val uiState: StateFlow<PageReadingUiState> = _uiState.asStateFlow()

    /**
     * Loads all ayahs for the given page number, grouped by surah.
     */
    fun loadPage(pageNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                pageNumber = pageNumber
            )

            try {
                val ayahs = quranRepository.getAyahsByPage(pageNumber)
                val allPages = quranRepository.getAllPageNumbers()

                // Group ayahs by surahId and fetch surah details
                val groups = mutableListOf<SurahAyahGroup>()
                val ayahsBySurah = ayahs.groupBy { it.surahId }

                // Maintain order: iterate ayahs and build groups in order of appearance
                val seenSurahs = mutableSetOf<Int>()
                for (ayah in ayahs) {
                    if (ayah.surahId !in seenSurahs) {
                        seenSurahs.add(ayah.surahId)
                        val surah = quranRepository.getSurahById(ayah.surahId)
                        if (surah != null) {
                            groups.add(
                                SurahAyahGroup(
                                    surah = surah,
                                    ayahs = ayahsBySurah[ayah.surahId] ?: emptyList()
                                )
                            )
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    surahGroups = groups,
                    isLoading = false,
                    errorMessage = null,
                    totalPages = allPages.size
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load page: ${exception.message}"
                )
            }
        }
    }

    /**
     * Navigates to the next page.
     */
    fun nextPage() {
        val current = _uiState.value.pageNumber
        if (current < _uiState.value.totalPages) {
            loadPage(current + 1)
        }
    }

    /**
     * Navigates to the previous page.
     */
    fun previousPage() {
        val current = _uiState.value.pageNumber
        if (current > 1) {
            loadPage(current - 1)
        }
    }

    /**
     * Selects or deselects an ayah.
     */
    fun selectAyah(ayahId: Int?, surahId: Int? = null) {
        _uiState.value = _uiState.value.copy(
            selectedAyahId = ayahId,
            selectedAyahSurahId = surahId
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

