package com.example.quranapp

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.quranapp.ui.theme.QuranAppTheme
import android.Manifest
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quranapp.navigation.RootNavigationGraph
import com.example.quranapp.navigation.Screen
import com.example.quranapp.util.Constants.REQUEST_CODE

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity.kt"
    private lateinit var navHostController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startDestination: String
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(tag, "Permission is granted")
            startDestination = Screen.HomeRoute.route
        } else {
            Log.d(tag, "Permission is not granted")
            startDestination = Screen.PermissionRoute.route
        }
        setContent {
            QuranAppTheme {
                navHostController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RootNavigationGraph(
                        innerPadding = innerPadding,
                        navHostController = navHostController,
                        startDestination = startDestination,
                        requestStoragePermission = { requestStoragePermission() }
                    )
                }
            }
        }
    }

    private fun requestStoragePermission() {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                navHostController.navigate(Screen.HomeRoute.route)
            } else {
                Log.d(tag, "Permission is denied")
            }
        }
}