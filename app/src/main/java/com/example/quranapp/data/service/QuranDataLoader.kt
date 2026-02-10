package com.example.quranapp.data.service

import android.content.Context
import android.util.Log
import com.example.quranapp.data.database.dao.AyahDao
import com.example.quranapp.data.database.dao.SurahDao
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.data.model.QuranAyahJson
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
    private val ayahDao: AyahDao,
) {
    private val tag = "QuranDataLoader"

    suspend fun initializeDatabase() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "=== Database Initialization Started ===")
                Log.d(tag, "Checking if database needs initialization...")

                val surahCount = surahDao.getSurahCount()
                val ayahCount = ayahDao.getAyahCount()

                Log.d(tag, "Current database state - Surahs: $surahCount, Ayahs: $ayahCount")
                Log.d(tag, "Expected: 114 Surahs and ~6236 Ayahs")

                // Check if database is empty or incomplete (114 surahs and ~6236 ayahs in Quran)
                if (surahCount < 114 || ayahCount < 6000) {
                    Log.w(tag, "⚠ Database is incomplete or empty. Loading data from JSON...")
                    loadQuranDataFromJson()

                    // Verify after loading
                    val newSurahCount = surahDao.getSurahCount()
                    val newAyahCount = ayahDao.getAyahCount()

                    if (newSurahCount == 114 && newAyahCount >= 6000) {
                        Log.d(tag, "✓ Database successfully populated - Surahs: $newSurahCount, Ayahs: $newAyahCount")
                    } else {
                        Log.e(tag, "✗ Database population incomplete - Surahs: $newSurahCount, Ayahs: $newAyahCount")
                    }
                } else {
                    Log.d(tag, "✓ Database already contains complete Quran data")
                }

                Log.d(tag, "=== Database Initialization Completed ===")
            } catch (e: Exception) {
                Log.e(tag, "✗ Critical error during database initialization", e)
                throw e // Re-throw to allow upper layers to handle
            }
        }
    }

    private suspend fun loadQuranDataFromJson() {
        try {
            Log.d(tag, ">>> Reading JSON file from assets...")
            val jsonString = readJsonFromAssets("quran_data.json")
            
            if (jsonString.isBlank()) {
                throw IllegalStateException("JSON file is empty or could not be read")
            }
            
            Log.d(tag, ">>> JSON file read successfully (${jsonString.length} characters)")
            Log.d(tag, ">>> Parsing JSON data...")
            
            val gson = Gson()
            val quranDataArray = gson.fromJson(jsonString, Array<QuranAyahJson>::class.java)

            if (quranDataArray == null || quranDataArray.size == 0) {
                throw IllegalStateException("Failed to parse JSON data or data is empty")
            }
            
            val quranData: List<QuranAyahJson> = quranDataArray.toList()
            Log.d(tag, ">>> Parsed ${quranData.size} Ayahs from JSON")

            Log.d(tag, ">>> Converting and preparing data for database insertion...")

            // Group ayahs by surah number
            val ayahsBySurah = quranData.groupBy { it.surahNumber }
            Log.d(tag, ">>> Found ${ayahsBySurah.size} Surahs")

            val surahs = mutableListOf<Surah>()
            val ayahs = mutableListOf<Ayah>()

            ayahsBySurah.forEach { (surahNumber, ayahList) ->
                try {
                    // Validate Surah number
                    if (surahNumber < 1 || surahNumber > 114) {
                        Log.w(tag, "⚠ Invalid Surah number: $surahNumber, skipping...")
                        return@forEach
                    }
                    
                    // Get surah info from the first ayah of this surah
                    val firstAyah = ayahList.firstOrNull() ?: run {
                        Log.w(tag, "⚠ No ayahs found for Surah $surahNumber, skipping...")
                        return@forEach
                    }

                    // Create Surah entity
                    val surah = Surah(
                        id = surahNumber,
                        nameArabic = firstAyah.surahNameAr.trim(),
                        nameEnglish = firstAyah.surahNameEn.trim(),
                        totalVerses = ayahList.size,
                        startPage = firstAyah.page.toIntOrNull() ?: 1,
                        startJuzz = firstAyah.juzz
                    )
                    surahs.add(surah)

                    // Convert QuranAyahJson to Ayah entities
                    ayahList.forEach { ayahJson ->
                        try {
                            if (ayahJson.ayahText.isBlank()) {
                                Log.w(tag, "⚠ Empty Ayah text for Surah $surahNumber, Ayah ${ayahJson.ayahNumber}")
                            }
                            
                            val ayah = Ayah(
                                id = ayahJson.ayahNumber,
                                surahId = surahNumber,
                                text = ayahJson.ayahText.trim(),
                                juzz = ayahJson.juzz,
                                page = ayahJson.page.toIntOrNull() ?: 1,
                                lineStart = ayahJson.lineStart,
                                lineEnd = ayahJson.lineEnd
                            )
                            ayahs.add(ayah)
                        } catch (e: Exception) {
                            Log.e(tag, "Error processing Ayah ${ayahJson.ayahNumber} in Surah $surahNumber", e)
                        }
                    }
                    
                    if (surahNumber % 20 == 0) {
                        Log.d(tag, ">>> Processed $surahNumber/114 Surahs...")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error processing Surah $surahNumber", e)
                }
            }

            if (surahs.isEmpty() || ayahs.isEmpty()) {
                throw IllegalStateException("No valid Surahs or Ayahs were processed from JSON")
            }

            Log.d(tag, ">>> Inserting ${surahs.size} Surahs into database...")
            surahDao.insertSurahs(surahs)
            Log.d(tag, ">>> Surahs inserted successfully")

            Log.d(tag, ">>> Inserting ${ayahs.size} Ayahs into database...")
            ayahDao.insertAyahs(ayahs)
            Log.d(tag, ">>> Ayahs inserted successfully")

            Log.d(tag, "✓✓✓ Database population completed successfully!")
            Log.d(tag, "✓ Total Surahs: ${surahs.size}")
            Log.d(tag, "✓ Total Ayahs: ${ayahs.size}")

        } catch (e: Exception) {
            Log.e(tag, "✗✗✗ Critical error loading Quran data from JSON", e)
            Log.e(tag, "Error details: ${e.message}")
            Log.e(tag, "Stack trace: ", e)
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
