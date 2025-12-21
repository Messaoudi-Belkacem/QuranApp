package com.example.quranapp.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "ayahs",
    primaryKeys = ["id", "surahId"],
    foreignKeys = [ForeignKey(
        entity = Surah::class,
        parentColumns = ["id"],
        childColumns = ["surahId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Ayah(
    val id: Int,
    val surahId: Int,
    val text: String
)