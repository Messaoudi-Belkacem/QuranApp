package com.example.quranapp.presentation.screen.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ImportContacts
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
            },
            onNavigateToPageReading = { pageNumber ->
                rootNavController.navigate(Screen.PageReadingRoute.createRoute(pageNumber))
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

    NavigationBar(
        tonalElevation = 2.dp,
        containerColor = NavigationBarDefaults.containerColor
    ) {
        screens.forEach { screen ->
            val isSelected = currentDestination?.route == screen.route
            val title = stringResource(id = screen.titleResId)
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.08f else 1f,
                animationSpec = tween(durationMillis = 220),
                label = "${screen.route}_icon_scale"
            )
            val labelAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.82f,
                animationSpec = tween(durationMillis = 220),
                label = "${screen.route}_label_alpha"
            )
            val iconYOffset by animateDpAsState(
                targetValue = if (isSelected) (-2).dp else 0.dp,
                animationSpec = tween(durationMillis = 220),
                label = "${screen.route}_icon_offset"
            )
            NavigationBarItem(
                selected = isSelected,
                onClick = { navigateToScreen(navController, screen.route) },
                icon = {
                    Icon(
                        imageVector = bottomBarIcon(screen = screen, selected = isSelected),
                        modifier = Modifier
                            .size(24.dp)
                            .offset(y = iconYOffset)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        contentDescription = title
                    )
                },
                label = {
                    Text(
                        text = title,
                        modifier = Modifier.graphicsLayer { alpha = labelAlpha }
                    )
                }
            )
        }
    }
}

private fun bottomBarIcon(screen: Screen, selected: Boolean): ImageVector {
    return when (screen) {
        Screen.HomeRoute -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        Screen.QuranRoute -> if (selected) Icons.Filled.ImportContacts else Icons.Outlined.ImportContacts
        Screen.QiblaRoute -> if (selected) Icons.Filled.Explore else Icons.Outlined.Explore
        Screen.MoreRoute -> if (selected) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz
        else -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
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
