package com.example.quranapp

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.Repository
import com.example.quranapp.util.createFolder
import com.example.quranapp.util.createMediaStoreFolder
import com.example.quranapp.util.isFolderExists
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(private val repository: Repository, private val context: Context) : ViewModel() {
    private val tag: String = "SharedViewModel.kt"

    init {
        Log.d(tag, "SharedViewModel init is called")
        viewModelScope.launch {
            val apiLevel = Build.VERSION.SDK_INT
            Log.d("API Level", "The API level of this device is: $apiLevel")
            if (repository.isFirstLaunch()) {
                Log.d(tag, "This is first launch")
                repository.setFirstLaunch(false)
            } else {
                Log.d(tag, "This is NOT first launch")
            }
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    if (createMediaStoreFolder(context, "Quran")) {
                        // Folder created successfully
                        Log.d(tag, "Folder created successfully")
                    } else {
                        // Folder creation failed
                        Log.d(tag, "Folder creation failed")
                    }
                }
                else -> {
                    val externalFolderPath = context.getExternalFilesDir(null)?.absolutePath + "Quran"
                    if (isFolderExists(externalFolderPath)) {
                        Log.d(tag, "Folder exists and path is $externalFolderPath")
                    } else {
                        Log.d(tag, "Folder does not exist")
                        if (createFolder(externalFolderPath)) {
                            Log.d(tag, "Folder created")
                        } else {
                            Log.d(tag, "Folder creation failed")
                        }
                    }
                }
            }
        }
    }
}