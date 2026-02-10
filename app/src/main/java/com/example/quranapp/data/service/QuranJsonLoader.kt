package com.example.quranapp.data.service

import android.content.Context
import android.util.Log
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
class QuranJsonLoader @Inject constructor(
    private val context: Context
) {
    private val tag = "QuranJsonLoader"

    // Cache the loaded data in memory
    private var cachedSurahs: List<Surah>? = null
    private var cachedAyahs: Map<Int, List<Ayah>>? = null

    suspend fun loadAllSurahs(): List<Surah> {
        return withContext(Dispatchers.IO) {
            try {
                // Return cached data if available
                if (cachedSurahs != null) {
                    Log.d(tag, "✓ Returning cached Surahs (${cachedSurahs!!.size} surahs)")
                    return@withContext cachedSurahs!!
                }

                Log.d(tag, "=== Loading Quran Data from JSON ===")
                loadAndParseQuranData()
                cachedSurahs ?: emptyList()
            } catch (e: Exception) {
                Log.e(tag, "✗✗✗ Error loading Quran data from JSON", e)
                throw e
            }
        }
    }

    private fun loadAndParseQuranData() {
        val jsonString = readJsonFromAssets("quran_data.json")

        if (jsonString.isBlank()) {
            throw IllegalStateException("JSON file is empty or could not be read")
        }

        Log.d(tag, ">>> JSON file read successfully (${jsonString.length} characters)")
        Log.d(tag, ">>> Parsing JSON data...")

        val gson = Gson()
        val quranDataArray = gson.fromJson(jsonString, Array<QuranAyahJson>::class.java)

        if (quranDataArray == null || quranDataArray.isEmpty()) {
            throw IllegalStateException("Failed to parse JSON data or data is empty")
        }

        val quranData = quranDataArray.toList()
        Log.d(tag, ">>> Parsed ${quranData.size} Ayahs from JSON")

        // Group ayahs by surah number
        val ayahsBySurah = quranData.groupBy { it.surahNumber }

        val surahs = mutableListOf<Surah>()
        val ayahsMap = mutableMapOf<Int, MutableList<Ayah>>()

        ayahsBySurah.forEach { (surahNumber, ayahJsonList) ->
            try {
                // Get surah info from the first ayah of this surah
                val firstAyah = ayahJsonList.first()

                val surah = Surah(
                    id = surahNumber,
                    nameArabic = firstAyah.surahNameAr.trim(),
                    nameEnglish = firstAyah.surahNameEn.trim(),
                    totalVerses = ayahJsonList.size,
                    startPage = firstAyah.page.toIntOrNull() ?: 1,
                    startJuzz = firstAyah.juzz
                )
                surahs.add(surah)

                // Convert QuranAyahJson to Ayah entities
                val ayahsList = ayahJsonList.map { ayahJson ->
                    Ayah(
                        id = ayahJson.ayahNumber,
                        surahId = surahNumber,
                        text = ayahJson.ayahText.trim(),
                        juzz = ayahJson.juzz,
                        page = ayahJson.page.toIntOrNull() ?: 1,
                        lineStart = ayahJson.lineStart,
                        lineEnd = ayahJson.lineEnd
                    )
                }.toMutableList()

                ayahsMap[surahNumber] = ayahsList
            } catch (e: Exception) {
                Log.e(tag, "Error processing Surah $surahNumber", e)
            }
        }

        // Sort surahs by id
        surahs.sortBy { it.id }

        // Cache the data
        cachedSurahs = surahs
        cachedAyahs = ayahsMap

        Log.d(tag, "✓✓✓ Data loaded and cached successfully!")
        Log.d(tag, "✓ Total Surahs: ${surahs.size}")
        Log.d(tag, "✓ Total Ayahs: ${ayahsMap.values.sumOf { it.size }}")
    }

    suspend fun getSurahById(surahId: Int): Surah? {
        return withContext(Dispatchers.IO) {
            val surahs = loadAllSurahs()
            surahs.find { it.id == surahId }
        }
    }

    suspend fun getAyahsBySurah(surahId: Int): List<Ayah> {
        return withContext(Dispatchers.IO) {
            // Ensure data is loaded
            if (cachedAyahs == null) {
                loadAllSurahs()
            }
            cachedAyahs?.get(surahId) ?: emptyList()
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

