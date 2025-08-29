package com.example.quranapp.domain.usecase

import com.example.quranapp.data.database.entities.Bookmark
import com.example.quranapp.data.repository.QuranRepository
import jakarta.inject.Inject

class BookmarkUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    suspend fun addBookmark(surahId: Int, ayahId: Int, note: String = "") {
        repository.addBookmark(surahId, ayahId, note)
    }

    suspend fun removeBookmark(bookmark: Bookmark) {
        repository.removeBookmark(bookmark)
    }

    suspend fun isBookmarked(surahId: Int, ayahId: Int): Boolean {
        return repository.isBookmarked(surahId, ayahId)
    }
}