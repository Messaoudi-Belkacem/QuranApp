package com.example.quranapp.data.model

import com.google.gson.annotations.SerializedName

data class QuranData(
    @SerializedName("surahs")
    val surahs: List<SurahJson>
)

data class SurahJson(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("name_arabic")
    val nameArabic: String,
    @SerializedName("name_english")
    val nameEnglish: String,
    @SerializedName("ayah_count")
    val ayahCount: Int,
    @SerializedName("revelation_type")
    val revelationType: String,
    @SerializedName("order_in_quran")
    val orderInQuran: Int,
    @SerializedName("ayahs")
    val ayahs: List<AyahJson>
)

data class AyahJson(
    @SerializedName("id")
    val id: Int,
    @SerializedName("ayah_number")
    val ayahNumber: Int,
    @SerializedName("text_arabic")
    val textArabic: String,
    @SerializedName("text_translation")
    val textTranslation: String,
    @SerializedName("juz_number")
    val juzNumber: Int,
    @SerializedName("hizb_number")
    val hizbNumber: Int,
    @SerializedName("ruku_number")
    val rukuNumber: Int
)
