package com.example.quranapp.data.service

import android.content.Context
import android.util.Log
import com.example.quranapp.data.database.dao.AyahDao
import com.example.quranapp.data.database.dao.SurahDao
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.data.model.SurahJson
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranDataLoader @Inject constructor(
    private val context: Context,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao
) {
    private val tag = "QuranDataLoader"

    suspend fun initializeDatabase() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Checking if database needs initialization...")

                val surahCount = surahDao.getSurahCount()
                val ayahCount = ayahDao.getAyahCount()

                Log.d(tag, "Current database state - Surahs: $surahCount, Ayahs: $ayahCount")

                // Check if database is empty or incomplete (114 surahs and ~6236 ayahs in Quran)
                if (surahCount < 114 || ayahCount < 6000) {
                    Log.d(tag, "Database is incomplete. Loading data from JSON...")
                    loadQuranDataFromJson()
                } else {
                    Log.d(tag, "Database already contains complete Quran data")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error during database initialization", e)
            }
        }
    }

    private suspend fun loadQuranDataFromJson() {
        try {
            Log.d(tag, "Reading JSON file from assets...")
            val jsonString = readJsonFromAssets("quran_data.json")

            Log.d(tag, "Parsing JSON data...")
            val gson = Gson()
            val quranDataArray = gson.fromJson(jsonString, Array<SurahJson>::class.java)
            val quranData = quranDataArray.toList()

            Log.d(tag, "Converting and inserting data into database...")
            val surahs = mutableListOf<Surah>()
            val ayahs = mutableListOf<Ayah>()

            quranData.forEach { surahJson ->
                // Convert SurahJson to Surah entity
                val surah = Surah(
                    id = surahJson.id,
                    name = surahJson.name,
                    nameArabic = surahJson.name,
                    nameEnglish = "",
                    ayahCount = surahJson.totalVerses,
                    revelationType = surahJson.type
                )
                surahs.add(surah)

                // Convert AyahJson to Ayah entities
                surahJson.verses.forEach { ayahJson ->
                    val ayah = Ayah(
                        id = ayahJson.id,
                        surahId = surahJson.id,
                        ayahNumber = ayahJson.id,
                        textArabic = ayahJson.text,
                        textTranslation = "",
                        juzNumber = 0,
                        hizbNumber = 0,
                        rukuNumber = 0
                    )
                    ayahs.add(ayah)
                }
            }

            Log.d(tag, "Inserting ${surahs.size} surahs into database...")
            surahDao.insertSurahs(surahs)

            Log.d(tag, "Inserting ${ayahs.size} ayahs into database...")
            ayahDao.insertAyahs(ayahs)

            Log.d(tag, "Database initialization completed successfully!")

        } catch (e: Exception) {
            Log.e(tag, "Error loading Quran data from JSON", e)
            throw e
        }
    }

    private fun readJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e(tag, "Error reading JSON file from assets", e)
            throw e
        }
    }
}
