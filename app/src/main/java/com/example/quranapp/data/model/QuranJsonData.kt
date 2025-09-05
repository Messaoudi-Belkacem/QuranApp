package com.example.quranapp.data.model

import com.google.gson.annotations.SerializedName

data class SurahJson(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("transliteration")
    val transliteration: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("total_verses")
    val totalVerses: Int,
    @SerializedName("verses")
    val verses: List<AyahJson>
)

data class AyahJson(
    @SerializedName("id")
    val id: Int,
    @SerializedName("text")
    val text: String,
)
