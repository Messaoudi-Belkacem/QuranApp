package com.example.quranapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single ayah entry from the new quran_data.json format.
 * Each entry contains both the ayah data and its associated surah info.
 */
data class QuranAyahJson(
    @SerializedName("id")
    val id: Int,
    @SerializedName("jozz")
    val juzz: Int,
    @SerializedName("page")
    val page: String,
    @SerializedName("sura_no")
    val surahNumber: Int,
    @SerializedName("sura_name_en")
    val surahNameEn: String,
    @SerializedName("sura_name_ar")
    val surahNameAr: String,
    @SerializedName("line_start")
    val lineStart: Int,
    @SerializedName("line_end")
    val lineEnd: Int,
    @SerializedName("aya_no")
    val ayahNumber: Int,
    @SerializedName("aya_text")
    val ayahText: String
)
