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

/**
 * Enum representing the different viewing modes for displaying Surah content.
 */
enum class ViewingMode {
    /** Traditional card-based list view where each ayah is displayed in a separate card */
    LIST,

    /** Continuous text view (Mushaf style) where ayahs flow together like a traditional Quran page */
    MUSHAF
}

/**
 * UI State for the Surah Reading Screen.
 *
 * @property currentSurah The currently loaded Surah details
 * @property allAyahs Complete list of ayahs in the current surah (unfiltered)
 * @property filteredAyahs Filtered list of ayahs based on search query
 * @property isLoading Whether the surah data is currently being loaded
 * @property errorMessage Error message to display if loading fails
 * @property searchQuery Current search query text
 * @property viewingMode Current viewing mode (LIST or MUSHAF)
 * @property selectedAyahId ID of the currently selected ayah in Mushaf mode
 */
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

/**
 * ViewModel for managing Surah Reading Screen state and business logic.
 * Handles loading surah data, search functionality, viewing modes, and ayah selection.
 */
@HiltViewModel
class SurahReadingViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    // ========================================
    // State Management
    // ========================================

    private val _uiState = MutableStateFlow(SurahReadingUiState())
    val uiState: StateFlow<SurahReadingUiState> = _uiState.asStateFlow()

    // ========================================
    // Data Loading
    // ========================================

    /**
     * Loads a Surah by its ID from the repository.
     * Updates the UI state with loading status, success data, or error message.
     *
     * @param surahId The ID of the surah to load (1-114)
     */
    fun loadSurah(surahId: Int) {
        viewModelScope.launch {
            // Set loading state
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Fetch surah data from repository
                val surah = quranRepository.getSurahById(surahId)
                val ayahs = quranRepository.getAyahsBySurah(surahId)

                // Update state with loaded data
                _uiState.value = _uiState.value.copy(
                    currentSurah = surah,
                    allAyahs = ayahs,
                    filteredAyahs = ayahs,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (exception: Exception) {
                // Handle loading error
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load Surah: ${exception.message}"
                )
            }
        }
    }

    /**
     * Reloads the current surah data.
     * Useful for retry functionality after an error.
     */
    fun refreshSurah(surahId: Int) {
        loadSurah(surahId)
    }

    // ========================================
    // Search Functionality
    // ========================================

    /**
     * Updates the search query and filters ayahs accordingly.
     * Searches in both Arabic text and ayah numbers.
     *
     * @param query The search text to filter ayahs
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterAyahs(query)
    }

    /**
     * Filters the ayahs list based on the search query.
     * If query is blank, shows all ayahs.
     * Otherwise, filters by Arabic text or ayah number.
     */
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

    /**
     * Clears the search query and shows all ayahs.
     */
    fun clearSearch() {
        updateSearchQuery("")
    }

    // ========================================
    // Viewing Mode Management
    // ========================================

    /**
     * Toggles between LIST and MUSHAF viewing modes.
     * Clears selected ayah when switching modes.
     */
    fun toggleViewingMode() {
        val newMode = when (_uiState.value.viewingMode) {
            ViewingMode.LIST -> ViewingMode.MUSHAF
            ViewingMode.MUSHAF -> ViewingMode.LIST
        }
        _uiState.value = _uiState.value.copy(
            viewingMode = newMode,
            selectedAyahId = null // Clear selection when switching modes
        )
    }

    // ========================================
    // Ayah Selection (Mushaf Mode)
    // ========================================

    /**
     * Selects or deselects an ayah in Mushaf viewing mode.
     * Used to show/hide ayah actions tooltip.
     *
     * @param ayahId The ID of the ayah to select, or null/-1 to deselect
     */
    fun selectAyah(ayahId: Int?) {
        _uiState.value = _uiState.value.copy(selectedAyahId = ayahId)
    }

    // ========================================
    // Error Management
    // ========================================

    /**
     * Clears any error message from the UI state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
