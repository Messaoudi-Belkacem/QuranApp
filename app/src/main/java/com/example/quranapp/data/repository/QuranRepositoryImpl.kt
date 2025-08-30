package com.example.quranapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.quranapp.data.database.dao.AyahDao
import com.example.quranapp.data.database.dao.BookmarkDao
import com.example.quranapp.data.database.dao.SurahDao
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Bookmark
import com.example.quranapp.data.database.entities.Surah
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import androidx.core.content.edit

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val bookmarkDao: BookmarkDao,
    private val context: Context,
) : QuranRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)
    }

    override fun getAllSurahs(): Flow<List<Surah>> = surahDao.getAllSurahs()

    override fun getSurahById(surahId: Int): Flow<Surah> = surahDao.getSurahById(surahId)

    override fun getAyahsBySurah(surahId: Int): Flow<List<Ayah>> =
        ayahDao.getAyahsBySurah(surahId)

    override suspend fun addBookmark(surahId: Int, ayahId: Int, note: String) {
        bookmarkDao.insertBookmark(Bookmark(surahId = surahId, ayahId = ayahId, note = note))
    }

    override suspend fun isBookmarked(surahId: Int, ayahId: Int): Boolean {
        return bookmarkDao.isBookmarked(surahId, ayahId) > 0
    }

    override suspend fun removeBookmark(bookmark: Bookmark) {
        bookmarkDao.deleteBookmark(bookmark = bookmark)
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()

    override suspend fun getSurahCount(): Int = surahDao.getSurahCount()

    override suspend fun getAyahCount(): Int = ayahDao.getAyahCount()

    override suspend fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    override suspend fun setFirstLaunch(isFirst: Boolean) {
        prefs.edit { putBoolean("is_first_launch", isFirst) }
    }
}