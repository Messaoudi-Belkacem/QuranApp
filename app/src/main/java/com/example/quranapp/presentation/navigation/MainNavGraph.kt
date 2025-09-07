package com.example.quranapp.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quranapp.presentation.screen.home.HomeScreen
import com.example.quranapp.presentation.screen.qibla.QiblaScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomeRoute.route
    ) {
        composable(route = Screen.HomeRoute.route) {
            HomeScreen(
                innerPadding = innerPadding,
                navHostController = navController
            )
        }
        composable(route = Screen.QiblaRoute.route) {
            QiblaScreen()
        }
    }
}