package com.example.quranapp.presentation.screen.main

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quranapp.R
import com.example.quranapp.presentation.navigation.MainNavGraph
import com.example.quranapp.presentation.navigation.Screen

@Composable
fun MainScreen(
    rootNavController: NavHostController
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) { innerPadding ->
        MainNavGraph(
            navController = navController,
            innerPadding = innerPadding,
            rootNavController = rootNavController,
            onNavigateToSurahReading = { surahId ->
                rootNavController.navigate(Screen.SurahReadingRoute.createRoute(surahId))
            }
        )
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = remember {
        listOf(
            Screen.HomeRoute,
            Screen.QuranRoute,
            Screen.QiblaRoute,
            Screen.MoreRoute
        )
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentDestination?.route == screen.route,
                onClick = { navigateToScreen(navController, screen.route) },
                icon = {
                    Icon(
                        painter = painterResource(id = screen.icon ?: R.drawable.mosque_vector),
                        modifier = Modifier.size(24.dp),
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(screen.title)
                }
            )
        }
    }
}

private fun navigateToScreen(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
