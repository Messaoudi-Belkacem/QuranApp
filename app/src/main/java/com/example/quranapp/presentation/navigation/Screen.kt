package com.example.quranapp.presentation.navigation

sealed class Screen(val route: String) {
    data object HomeRoute: Screen(route = "home_screen")
    data object PermissionRoute: Screen(route = "permission_screen")
    data object MushafRoute: Screen(route = "mushaf_screen")
    data object PrayerTimesRoute: Screen(route = "prayer_times_screen")
    data object AdkarRoute: Screen(route = "adkar_screen")
    data object SettingsRoute: Screen(route = "settings_screen")
}