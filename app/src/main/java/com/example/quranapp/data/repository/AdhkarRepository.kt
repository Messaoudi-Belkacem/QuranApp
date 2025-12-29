package com.example.quranapp.data.repository

import android.content.Context
import com.example.quranapp.data.model.AdhkarCategory
import com.example.quranapp.data.model.AdhkarCategoryItem
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdhkarRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val categories = listOf(
        AdhkarCategoryItem(
            id = "morning",
            title = "أذكار الصباح",
            fileName = "adhkar_morning.json",
            icon = "🌅"
        ),
        AdhkarCategoryItem(
            id = "evening",
            title = "أذكار المساء",
            fileName = "adhkar_evening.json",
            icon = "🌙"
        ),
        AdhkarCategoryItem(
            id = "post_prayer",
            title = "أذكار بعد الصلاة",
            fileName = "adhkar_post_prayer.json",
            icon = "🕌"
        )
    )

    suspend fun getCategories(): List<AdhkarCategoryItem> = withContext(Dispatchers.IO) {
        categories
    }

    suspend fun getCategoryAdhkar(fileName: String): AdhkarCategory? = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("adhkar/$fileName").bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, AdhkarCategory::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

