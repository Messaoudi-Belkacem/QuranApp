package com.example.quranapp.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quranapp.presentation.navigation.Screen
import com.example.quranapp.util.addPaddingValues

data class HomeOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    navHostController: NavHostController
) {
    val newPadding = addPaddingValues(innerPadding, PaddingValues(24.dp))

    val options = listOf(
        HomeOption(
            title = "Mushaf",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = { navHostController.navigate(Screen.QuranRoute.route) }
        ),
        HomeOption(
            title = "Prayer Times",
            icon = Icons.Default.AccessTime,
            onClick = { navHostController.navigate(Screen.PrayerTimesRoute.route) }
        ),
        HomeOption(
            title = "Adkar",
            icon = Icons.Default.Favorite,
            onClick = { navHostController.navigate(Screen.AdkarRoute.route) }
        ),
        HomeOption(
            title = "Settings",
            icon = Icons.Default.Settings,
            onClick = { navHostController.navigate(Screen.SettingsRoute.route) }
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = newPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        items(options) { option ->
            HomeOptionItem(option = option)
        }
    }
}

@Composable
fun HomeOptionItem(option: HomeOption) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.title,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = option.title)
    }
}
