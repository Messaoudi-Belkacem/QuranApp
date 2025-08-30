package com.example.quranapp.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quranapp.presentation.screen.adkar.AdkarScreen
import com.example.quranapp.presentation.screen.home.HomeScreen
import com.example.quranapp.presentation.screen.mushaf.MushafScreen
import com.example.quranapp.presentation.screen.permission.PermissionScreen
import com.example.quranapp.presentation.screen.prayertimes.PrayerTimesScreen
import com.example.quranapp.presentation.screen.settings.SettingsScreen

@Composable
fun RootNavigationGraph(
    innerPadding: PaddingValues,
    navHostController: NavHostController,
    startDestination: String
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
        composable(route = Screen.HomeRoute.route) {
            HomeScreen(
                innerPadding = innerPadding,
                navHostController = navHostController
            )
        }
        composable(route = Screen.PermissionRoute.route) {
            PermissionScreen(
                innerPadding = innerPadding,
                navHostController = navHostController
            )
        }
        composable(route = Screen.MushafRoute.route) {
            MushafScreen()
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
    }
}