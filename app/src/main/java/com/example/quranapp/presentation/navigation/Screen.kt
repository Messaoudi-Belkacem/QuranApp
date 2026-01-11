package com.example.quranapp.presentation.navigation

import com.example.quranapp.R

sealed class Screen(val route: String, val titleResId: Int, val icon: Int? = null) {
    data object HomeRoute : Screen(
        route = "home_screen",
        titleResId = R.string.home,
        icon = R.drawable.ic_home
    )

    data object QuranRoute : Screen(
        route = "quran_screen",
        titleResId = R.string.quran,
        icon = R.drawable.ic_quran
    )

    data object QiblaRoute : Screen(
        route = "qibla_screen",
        titleResId = R.string.qibla,
        icon = R.drawable.ic_qibla
    )

    data object TasbihRoute : Screen(
        route = "tasbih_screen",
        titleResId = R.string.tasbih,
        icon = R.drawable.ic_tasbih
    )

    data object MoreRoute : Screen(
        route = "more_screen",
        titleResId = R.string.more,
        icon = android.R.drawable.ic_menu_more
    )

    data object SurahReadingRoute : Screen(route = "surah_reading_screen/{surahId}", titleResId = 0) {
        fun createRoute(surahId: Int) = "surah_reading_screen/$surahId"
    }

    data object PrayerTimesRoute : Screen(route = "prayer_times_screen", titleResId = 0)
    data object AdkarRoute : Screen(route = "adkar_screen", titleResId = 0)
    data object SettingsRoute : Screen(route = "settings_screen", titleResId = 0)
    data object HelpRoute : Screen(route = "help_screen", titleResId = 0)
    data object AboutRoute : Screen(route = "about_screen", titleResId = 0)

    data object MainRoute : Screen(route = "main_screen", titleResId = R.string.main)

}