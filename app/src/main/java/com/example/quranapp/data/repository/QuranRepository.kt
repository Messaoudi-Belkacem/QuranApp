package com.example.quranapp.data.repository

import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Bookmark
import com.example.quranapp.data.database.entities.Surah
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getAllSurahs(): Flow<List<Surah>>
    fun getAyahsBySurah(surahId: Int): Flow<List<Ayah>>
    suspend fun addBookmark(surahId: Int, ayahId: Int, note: String)
    suspend fun isBookmarked(surahId: Int, ayahId: Int): Boolean
    fun getAllBookmarks(): Flow<List<Bookmark>>
}