package com.example.quranapp.util

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import java.io.File

fun addPaddingValues(original: PaddingValues, additional: PaddingValues): PaddingValues {
    return PaddingValues(
        start = original.calculateStartPadding(LayoutDirection.Ltr) + additional.calculateStartPadding(LayoutDirection.Ltr),
        top = original.calculateTopPadding() + additional.calculateTopPadding(),
        end = original.calculateEndPadding(LayoutDirection.Ltr) + additional.calculateEndPadding(LayoutDirection.Ltr),
        bottom = original.calculateBottomPadding() + additional.calculateBottomPadding()
    )
}

fun isFolderExists(folderPath: String): Boolean {
    val folder = File(folderPath)
    return folder.exists() && folder.isDirectory
}

fun createFolder(folderPath: String): Boolean {
    try {
        val folder = File(folderPath)
        folder.mkdirs()
        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

fun createMediaStoreFolder(context: Context, folderName: String): Boolean {
    // Create values for the new folder
    val values = ContentValues().apply {
        put(MediaStore.Audio.Media.RELATIVE_PATH, "Audiobooks/$folderName")
        put(MediaStore.Audio.Media.IS_PENDING, 1)
    }

    // Insert the new folder into MediaStore
    val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

    // Check if the folder was created successfully
    if (uri != null) {
        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0) // Mark as no longer pending
        context.contentResolver.update(uri, values, null, null)
        return true // Folder created successfully
    }
    return false // Folder creation failed
}