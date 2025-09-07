package com.example.quranapp.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quranapp.presentation.screen.adkar.AdkarScreen
import com.example.quranapp.presentation.screen.main.MainScreen
import com.example.quranapp.presentation.screen.permission.PermissionScreen
import com.example.quranapp.presentation.screen.prayertimes.PrayerTimesScreen
import com.example.quranapp.presentation.screen.quran.QuranScreen
import com.example.quranapp.presentation.screen.settings.SettingsScreen
import com.example.quranapp.presentation.screen.surah.SurahReadingScreen

@Composable
fun RootNavigationGraph(
    navHostController: NavHostController,
    startDestination: String,
) {
    val time = 250
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(time)) },
        exitTransition = { fadeOut(animationSpec = tween(time)) },
        popEnterTransition = { fadeIn(animationSpec = tween(time)) },
        popExitTransition = { fadeOut(animationSpec = tween(time)) }
    ) {
        composable(route = Screen.PermissionRoute.route) {
            PermissionScreen(
                navHostController = navHostController
            )
        }
        composable(route = Screen.QuranRoute.route) {
            QuranScreen(
                onSurahClick = { surah ->
                    navHostController.navigate(Screen.SurahReadingRoute.createRoute(surah.id))
                }
            )
        }
        composable(
            route = Screen.SurahReadingRoute.route,
            arguments = listOf(navArgument("surahId") { type = NavType.IntType })
        ) { backStackEntry ->
            val surahId = backStackEntry.arguments?.getInt("surahId") ?: 1
            SurahReadingScreen(
                surahId = surahId,
                onBackClick = { navHostController.popBackStack() }
            )
        }
        composable(route = Screen.PrayerTimesRoute.route) {
            PrayerTimesScreen()
        }
        composable(route = Screen.AdkarRoute.route) {
            AdkarScreen()
        }
        composable(route = Screen.SettingsRoute.route) {
            SettingsScreen()
        }
        composable(route = Screen.MainRoute.route) {
            MainScreen()
        }
    }
}