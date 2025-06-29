package com.example.quranapp.domain.usecase

import com.example.quranapp.data.repository.QuranRepository
import jakarta.inject.Inject

class BookmarkUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    suspend fun addBookmark(surahId: Int, ayahId: Int, note: String = "") {
        repository.addBookmark(surahId, ayahId, note)
    }

    suspend fun removeBookmark(surahId: Int, ayahId: Int) {
        repository.removeBookmark(surahId, ayahId)
    }

    suspend fun isBookmarked(surahId: Int, ayahId: Int): Boolean {
        return repository.isBookmarked(surahId, ayahId)
    }
}