package com.example.quranapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quranapp.presentation.navigation.RootNavigationGraph
import com.example.quranapp.presentation.navigation.Screen
import com.example.quranapp.presentation.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = MainActivity::class.java.simpleName
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var navHostController: NavHostController

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                Log.d(tag, "Location permission granted (fine: $fineLocationGranted, coarse: $coarseLocationGranted)")
                navHostController.navigate(Screen.MainRoute.route) {
                    popUpTo(0) // Clear back stack
                }
            } else {
                Log.d(tag, "Location permissions denied")
                navHostController.navigate(Screen.PermissionRoute.route) {
                    popUpTo(0)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
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

        val startDestination: String = if (hasFineLocation || hasCoarseLocation) {
            Log.d(tag, "Location permission already granted (fine: $hasFineLocation, coarse: $hasCoarseLocation)")
            Screen.MainRoute.route
        } else {
            Log.d(tag, "Location permissions not granted, requesting...")
            // Request both permissions
            requestMultiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            Screen.PermissionRoute.route
        }

        setContent {
            AppTheme {
                navHostController = rememberNavController()
                RootNavigationGraph(
                    navHostController = navHostController,
                    startDestination = startDestination
                )
            }
        }
    }
}