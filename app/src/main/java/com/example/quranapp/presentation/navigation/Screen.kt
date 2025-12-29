package com.example.quranapp.presentation.navigation

import com.example.quranapp.R

sealed class Screen(val route: String, val title: String, val icon: Int? = null) {
    data object HomeRoute : Screen(
        route = "home_screen",
        title = "Home",
        icon = R.drawable.ic_home
    )

    data object QuranRoute : Screen(
        route = "quran_screen",
        title = "Quran",
        icon = R.drawable.ic_quran
    )

    data object QiblaRoute : Screen(
        route = "qibla_screen",
        title = "Qibla",
        icon = R.drawable.ic_qibla
    )

    data object TasbihRoute : Screen(
        route = "tasbih_screen",
        title = "Tasbih",
        icon = R.drawable.ic_tasbih
    )

    data object MoreRoute : Screen(
        route = "more_screen",
        title = "More",
        icon = android.R.drawable.ic_menu_more
    )


    data object PermissionRoute : Screen(route = "permission_screen", title = "")
    data object SurahReadingRoute : Screen(route = "surah_reading_screen/{surahId}", title = "") {
        fun createRoute(surahId: Int) = "surah_reading_screen/$surahId"
    }

    data object PrayerTimesRoute : Screen(route = "prayer_times_screen", title = "")
    data object AdkarRoute : Screen(route = "adkar_screen", title = "")
    data object SettingsRoute : Screen(route = "settings_screen", title = "")
    data object HelpRoute : Screen(route = "help_screen", title = "")
    data object AboutRoute : Screen(route = "about_screen", title = "")

    data object MainRoute : Screen(route = "main_screen", title = "Main")

}