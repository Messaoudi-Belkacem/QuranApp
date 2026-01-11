package com.example.quranapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quranapp.presentation.navigation.RootNavigationGraph
import com.example.quranapp.presentation.navigation.Screen
import com.example.quranapp.presentation.ui.theme.AppTheme
import com.example.quranapp.util.LanguageManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = MainActivity::class.java.simpleName
    private lateinit var navHostController: NavHostController

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                Log.d(tag, "Location permission granted (fine: $fineLocationGranted, coarse: $coarseLocationGranted)")
            } else {
                Log.d(tag, "Location permissions denied")
            }
        }

    override fun attachBaseContext(newBase: Context) {
        LanguageManager.applyLanguage(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved language before creating the activity
        LanguageManager.applyLanguage(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if either fine or coarse location permission is granted
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            Log.d(tag, "Location permission already granted (fine: $hasFineLocation, coarse: $hasCoarseLocation)")
        } else {
            Log.d(tag, "Location permissions not granted, requesting...")
            // Request both permissions
            requestMultiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        setContent {
            AppTheme {
                navHostController = rememberNavController()
                RootNavigationGraph(
                    navHostController = navHostController,
                    startDestination = Screen.MainRoute.route
                )
            }
        }
    }
}