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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
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

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d(tag, "Permission granted")
            navHostController.navigate(Screen.MainRoute.route) {
                popUpTo(0) // Clear back stack
            }
        } else {
            Log.d(tag, "Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startDestination: String = if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(tag, "Permission is granted")
            Screen.MainRoute.route
        } else {
            Log.d(tag, "Permission is not granted")
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            Screen.PermissionRoute.route
        }

        val apiLevel = android.os.Build.VERSION.SDK_INT
        val adjustedStartDestination = if (apiLevel >= 34 && startDestination == Screen.PermissionRoute.route) {
            Screen.MainRoute.route
        } else {
            startDestination
        }

        setContent {
            AppTheme {
                navHostController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RootNavigationGraph(
                        innerPadding = innerPadding,
                        navHostController = navHostController,
                        startDestination = adjustedStartDestination
                    )
                }
            }
        }
    }
}