package com.example.quranapp.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quranapp.presentation.screen.about.AboutScreen
import com.example.quranapp.presentation.screen.adhkar.AdhkarScreen
import com.example.quranapp.presentation.screen.help.HelpScreen
import com.example.quranapp.presentation.screen.main.MainScreen
import com.example.quranapp.presentation.screen.page.PageReadingScreen
import com.example.quranapp.presentation.screen.prayertimes.PrayerTimesScreen
import com.example.quranapp.presentation.screen.quran.QuranScreen
import com.example.quranapp.presentation.screen.settings.SettingsScreen
import com.example.quranapp.presentation.screen.surah.SurahReadingScreen
import com.example.quranapp.presentation.screen.tasbih.TasbihScreen

@Composable
fun RootNavigationGraph(
    navHostController: NavHostController,
    startDestination: String,
) {
    val time = 300
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(time)) },
        exitTransition = { fadeOut(animationSpec = tween(time)) },
        popEnterTransition = { fadeIn(animationSpec = tween(time)) },
        popExitTransition = { fadeOut(animationSpec = tween(time)) }
    ) {
        composable(route = Screen.QuranRoute.route) {
            QuranScreen(
                onSurahClick = { surah ->
                    navHostController.navigate(Screen.SurahReadingRoute.createRoute(surah.id))
                },
                onPageClick = { pageNumber ->
                    navHostController.navigate(Screen.PageReadingRoute.createRoute(pageNumber))
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
        composable(
            route = Screen.PageReadingRoute.route,
            arguments = listOf(navArgument("pageNumber") { type = NavType.IntType })
        ) { backStackEntry ->
            val pageNumber = backStackEntry.arguments?.getInt("pageNumber") ?: 1
            PageReadingScreen(
                pageNumber = pageNumber,
                onBackClick = { navHostController.popBackStack() }
            )
        }
        composable(route = Screen.PrayerTimesRoute.route) {
            PrayerTimesScreen()
        }
        composable(route = Screen.AdkarRoute.route) {
            AdhkarScreen(
                innerPadding = PaddingValues(0.dp),
                onNavigateBack = { navHostController.popBackStack() }
            )
        }
        composable(route = Screen.TasbihRoute.route) {
            TasbihScreen(
                innerPadding = PaddingValues(0.dp),
                onNavigateBack = { navHostController.popBackStack() }
            )
        }
        composable(route = Screen.SettingsRoute.route) {
            SettingsScreen(
                onNavigateBack = { navHostController.popBackStack() }
            )
        }
        composable(route = Screen.HelpRoute.route) {
            HelpScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }
        composable(route = Screen.AboutRoute.route) {
            AboutScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }
        composable(route = Screen.MainRoute.route) {
            MainScreen(rootNavController = navHostController)
        }
    }
}