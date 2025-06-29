package com.example.quranapp.data.repository

import com.example.quranapp.data.database.dao.AyahDao
import com.example.quranapp.data.database.dao.BookmarkDao
import com.example.quranapp.data.database.dao.SurahDao
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Bookmark
import com.example.quranapp.data.database.entities.Surah
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val bookmarkDao: BookmarkDao
) : QuranRepository {

    override fun getAllSurahs(): Flow<List<Surah>> = surahDao.getAllSurahs()

    override fun getAyahsBySurah(surahId: Int): Flow<List<Ayah>> =
        ayahDao.getAyahsBySurah(surahId)

    override suspend fun addBookmark(surahId: Int, ayahId: Int, note: String) {
        bookmarkDao.insertBookmark(Bookmark(surahId = surahId, ayahId = ayahId, note = note))
    }

    override suspend fun isBookmarked(surahId: Int, ayahId: Int): Boolean {
        return bookmarkDao.isBookmarked(surahId, ayahId) > 0
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
}