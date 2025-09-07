package com.example.quranapp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object HomeRoute : Screen(
        route = "home_screen",
        title = "Home",
        icon = Icons.Filled.Home
    )

    data object QiblaRoute : Screen(
        route = "qibla_screen",
        title = "Qibla",
        icon = Icons.Filled.Explore
    )

    data object PermissionRoute : Screen(route = "permission_screen", title = "", icon = Icons.Filled.Home)
    data object SurahReadingRoute : Screen(route = "surah_reading_screen/{surahId}", title = "", icon = Icons.Filled.Home) {
        fun createRoute(surahId: Int) = "surah_reading_screen/$surahId"
    }
    data object PrayerTimesRoute : Screen(route = "prayer_times_screen", title = "", icon = Icons.Filled.Home)
    data object AdkarRoute : Screen(route = "adkar_screen", title = "", icon = Icons.Filled.Home)
    data object SettingsRoute : Screen(route = "settings_screen", title = "", icon = Icons.Filled.Home)

    data object MainRoute : Screen(route = "main_screen", title = "Main", icon = Icons.Filled.Home)
    data object TasbihRoute : Screen(
        route = "tasbih_screen",
        title = "Tasbih",
        icon = Icons.Filled.Favorite
    )

    data object QuranRoute : Screen(
        route = "quran_screen",
        title = "Quran",
        icon = Icons.Filled.MenuBook
    )
}