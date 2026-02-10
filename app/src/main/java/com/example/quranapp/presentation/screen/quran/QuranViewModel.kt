package com.example.quranapp.presentation.screen.quran

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.data.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MushafUiState(
    val surahs: List<Surah> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val tag = "QuranViewModel"
    private val _uiState = MutableStateFlow(MushafUiState())
    val uiState: StateFlow<MushafUiState> = _uiState.asStateFlow()

    init {
        Log.d(tag, "=== QuranViewModel initialized ===")
        loadSurahs()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            try {
                Log.d(tag, ">>> Starting to load Surahs from JSON...")
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val surahs = quranRepository.getAllSurahs()
                
                Log.d(tag, ">>> Loaded ${surahs.size} Surahs from JSON")
                if (surahs.isEmpty()) {
                    Log.w(tag, "⚠ Surahs list is empty!")
                } else {
                    Log.d(tag, "✓ First Surah: ${surahs.firstOrNull()?.nameEnglish} (${surahs.firstOrNull()?.nameArabic})")
                    Log.d(tag, "✓ Last Surah: ${surahs.lastOrNull()?.nameEnglish} (${surahs.lastOrNull()?.nameArabic})")
                }
                
                _uiState.value = _uiState.value.copy(
                    surahs = surahs,
                    isLoading = false,
                    errorMessage = null
                )
                Log.d(tag, "✓✓✓ UI State updated - isLoading: false, surahs count: ${surahs.size}")
            } catch (e: Exception) {
                Log.e(tag, "✗✗✗ Error loading Surahs", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load Surahs"
                )
            }
        }
    }

    fun refreshSurahs() {
        Log.d(tag, ">>> Refresh requested")
        loadSurahs()
    }
}
