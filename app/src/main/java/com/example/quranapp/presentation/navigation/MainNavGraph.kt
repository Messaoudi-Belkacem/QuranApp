package com.example.quranapp.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quranapp.presentation.screen.home.HomeScreen
import com.example.quranapp.presentation.screen.more.MoreScreen
import com.example.quranapp.presentation.screen.qibla.QiblaScreen
import com.example.quranapp.presentation.screen.quran.QuranScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onNavigateToSurahReading: (Int) -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomeRoute.route
    ) {
        composable(route = Screen.HomeRoute.route) {
            HomeScreen(
                innerPadding = innerPadding
            )
        }
        composable(route = Screen.QiblaRoute.route) {
            QiblaScreen(
                innerPadding = innerPadding
            )
        }
        composable(route = Screen.QuranRoute.route) {
            QuranScreen(
                onSurahClick = { surah ->
                    // Pass navigation up to parent level where SurahReadingRoute is defined
                    onNavigateToSurahReading(surah.id)
                }
            )
        }
        composable(route = Screen.MoreRoute.route) {
            MoreScreen(
                innerPadding = innerPadding
            )
        }
    }
}