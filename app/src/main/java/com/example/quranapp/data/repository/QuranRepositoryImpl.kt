package com.example.quranapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.quranapp.data.database.dao.BookmarkDao
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Bookmark
import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.data.model.Location
import com.example.quranapp.data.service.QuranJsonLoader
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import androidx.core.content.edit

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val context: Context,
    private val quranJsonLoader: QuranJsonLoader
) : QuranRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)
    }

    override suspend fun getAllSurahs(): List<Surah> = quranJsonLoader.loadAllSurahs()

    override suspend fun getSurahById(surahId: Int): Surah? = quranJsonLoader.getSurahById(surahId)

    override suspend fun getAyahsBySurah(surahId: Int): List<Ayah> = quranJsonLoader.getAyahsBySurah(surahId)

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

    override suspend fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    override suspend fun setFirstLaunch(isFirst: Boolean) {
        prefs.edit { putBoolean("is_first_launch", isFirst) }
    }

    override suspend fun getCurrentLocation(): Location? {
        val locationString = prefs.getString("current_location", null)
        return locationString?.let { Location.fromString(it) }
    }

    override suspend fun setCurrentLocation(latitude: Float, longitude: Float) {
        val location = Location(latitude, longitude)
        prefs.edit { putString("current_location", location.toString()) }
    }

    override suspend fun clearCurrentLocation() {
        prefs.edit { remove("current_location") }
    }

    override suspend fun getAyahsByPage(page: Int): List<Ayah> = quranJsonLoader.getAyahsByPage(page)

    override suspend fun getAllPageNumbers(): List<Int> = quranJsonLoader.getAllPageNumbers()
}

