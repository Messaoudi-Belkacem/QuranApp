package com.example.quranapp.data.model

import com.google.gson.annotations.SerializedName

data class AdhkarCategory(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: List<Dhikr>
)

data class Dhikr(
    @SerializedName("zekr")
    val text: String,
    @SerializedName("repeat")
    val repeat: Int,
    @SerializedName("bless")
    val benefit: String
)

data class AdhkarCategoryItem(
    val id: String,
    val title: String,
    val fileName: String,
    val icon: String = "☪️"
)

