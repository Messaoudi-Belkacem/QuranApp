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
    val pagesCache: Map<Int, List<SurahAyahGroup>> = emptyMap(),
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

    private val pagesCache = mutableMapOf<Int, List<SurahAyahGroup>>()

    /**
     * Loads all ayahs for the given page number, grouped by surah.
     */
    fun loadPage(pageNumber: Int) {
        val clampedPage = PageNavigationPolicy.clampPage(pageNumber)

        // If already in memory cache, update state immediately without loading spinner
        if (pagesCache.containsKey(clampedPage)) {
            _uiState.value = _uiState.value.copy(
                pageNumber = clampedPage,
                surahGroups = pagesCache[clampedPage] ?: emptyList(),
                pagesCache = pagesCache.toMap(),
                isLoading = false,
                errorMessage = null
            )
            prefetchAdjacent(clampedPage)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                pageNumber = clampedPage
            )

            try {
                val groups = fetchPageGroups(clampedPage)
                pagesCache[clampedPage] = groups

                _uiState.value = _uiState.value.copy(
                    pageNumber = clampedPage,
                    surahGroups = groups,
                    pagesCache = pagesCache.toMap(),
                    isLoading = false,
                    errorMessage = null
                )

                prefetchAdjacent(clampedPage)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load page: ${exception.message}"
                )
            }
        }
    }

    private suspend fun fetchPageGroups(page: Int): List<SurahAyahGroup> {
        val ayahs = quranRepository.getAyahsByPage(page)
        val groups = mutableListOf<SurahAyahGroup>()
        val ayahsBySurah = ayahs.groupBy { it.surahId }

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
        return groups
    }

    private fun prefetchAdjacent(centerPage: Int) {
        viewModelScope.launch {
            if (PageNavigationPolicy.hasPrevious(centerPage)) {
                val prevPage = PageNavigationPolicy.getPreviousPage(centerPage)
                if (!pagesCache.containsKey(prevPage)) {
                    val groups = fetchPageGroups(prevPage)
                    pagesCache[prevPage] = groups
                    _uiState.value = _uiState.value.copy(pagesCache = pagesCache.toMap())
                }
            }
            if (PageNavigationPolicy.hasNext(centerPage)) {
                val nextPage = PageNavigationPolicy.getNextPage(centerPage)
                if (!pagesCache.containsKey(nextPage)) {
                    val groups = fetchPageGroups(nextPage)
                    pagesCache[nextPage] = groups
                    _uiState.value = _uiState.value.copy(pagesCache = pagesCache.toMap())
                }
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

