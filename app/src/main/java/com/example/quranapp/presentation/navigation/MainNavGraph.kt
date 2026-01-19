package com.example.quranapp.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    rootNavController: NavHostController,
    onNavigateToSurahReading: (Int) -> Unit = {},
) {
    val time = 250
    NavHost(
        navController = navController,
        startDestination = Screen.HomeRoute.route,
        enterTransition = { fadeIn(animationSpec = tween(time)) },
        exitTransition = { fadeOut(animationSpec = tween(time)) },
        popEnterTransition = { fadeIn(animationSpec = tween(time)) },
        popExitTransition = { fadeOut(animationSpec = tween(time)) }
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
                innerPadding = innerPadding,
                navController = rootNavController
            )
        }
    }
}