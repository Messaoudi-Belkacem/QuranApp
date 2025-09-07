package com.example.quranapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quranapp.presentation.screen.home.HomeScreen
import com.example.quranapp.presentation.screen.qibla.QiblaScreen
import com.example.quranapp.presentation.screen.quran.QuranScreen
import com.example.quranapp.presentation.screen.tasbih.TasbihScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomeRoute.route
    ) {
        composable(route = Screen.HomeRoute.route) {
            HomeScreen(
                navHostController = navController
            )
        }
        composable(route = Screen.QiblaRoute.route) {
            QiblaScreen()
        }
        composable(route = Screen.QuranRoute.route) {
            QuranScreen()
        }
        composable(route = Screen.TasbihRoute.route) {
            TasbihScreen()
        }
    }
}