package com.example.quranapp.navigation

sealed class Screen(val route: String) {
    data object HomeRoute: Screen(route = "home_screen")
    data object PermissionRoute: Screen(route = "permission_screen")
}