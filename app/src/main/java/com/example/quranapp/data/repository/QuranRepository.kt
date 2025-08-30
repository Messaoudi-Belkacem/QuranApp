package com.example.quranapp.data.repository

import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Bookmark
import com.example.quranapp.data.database.entities.Surah
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getAllSurahs(): Flow<List<Surah>>
    fun getSurahById(surahId: Int): Flow<Surah>
    fun getAyahsBySurah(surahId: Int): Flow<List<Ayah>>
    suspend fun addBookmark(surahId: Int, ayahId: Int, note: String)
    suspend fun isBookmarked(surahId: Int, ayahId: Int): Boolean
    suspend fun removeBookmark(bookmark: Bookmark)
    fun getAllBookmarks(): Flow<List<Bookmark>>
    suspend fun getSurahCount(): Int
    suspend fun getAyahCount(): Int

    suspend fun isFirstLaunch(): Boolean
    suspend fun setFirstLaunch(isFirst: Boolean)
}