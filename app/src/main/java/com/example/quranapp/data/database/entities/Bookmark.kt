package com.example.quranapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahId: Int,
    val ayahId: Int,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)