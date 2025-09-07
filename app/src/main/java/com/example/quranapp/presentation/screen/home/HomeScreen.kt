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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quranapp.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.quranapp.util.addPaddingValues
import kotlinx.coroutines.delay

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
            title = "Favorites",
            icon = Icons.Filled.Favorite,
            onClick = { /* Future implementation for time-related features */ }
        ),
        HomeOption(
            title = "Settings",
            icon = Icons.Filled.Settings,
            onClick = { navHostController.navigate(Screen.SettingsRoute.route) }
        ),
        HomeOption(
            title = "Quran",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = { navHostController.navigate(Screen.QuranRoute.route) }
        ),
        HomeOption(
            title = "Time",
            icon = Icons.Filled.AccessTime,
            onClick = { /* Future implementation for time-related features */ }
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {
        CurrentTimeDisplay() // Add current time display at the top
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
}

@Composable
fun CurrentTimeDisplay() {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val currentTime = remember { mutableStateOf(timeFormat.format(Date())) }

    // Update time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = timeFormat.format(Date())
            delay(60_000L) // Delay for 1 minute
        }
    }

    Text(
        text = currentTime.value,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
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
