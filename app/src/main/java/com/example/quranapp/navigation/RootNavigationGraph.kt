package com.example.quranapp.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quranapp.screen.home.HomeScreen
import com.example.quranapp.screen.permission.PermissionScreen

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
            HomeScreen(innerPadding = innerPadding)
        }
        composable(route = Screen.PermissionRoute.route) {
            PermissionScreen(
                innerPadding = innerPadding,
                navHostController = navHostController
            )
        }
    }
}