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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.quranapp.util.addPaddingValues

data class HomeOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    innerPadding: PaddingValues
) {
    val newPadding = addPaddingValues(innerPadding, PaddingValues(24.dp))

    val options = listOf(
        HomeOption(
            title = "Mushaf",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = { /* TODO: Navigate to Mushaf screen */ }
        ),
        HomeOption(
            title = "Prayer Times",
            icon = Icons.Default.AccessTime,
            onClick = { /* TODO: Navigate to Prayer Times screen */ }
        ),
        HomeOption(
            title = "Adkar",
            icon = Icons.Default.Favorite,
            onClick = { /* TODO: Navigate to Adkar screen */ }
        ),
        HomeOption(
            title = "Settings",
            icon = Icons.Default.Settings,
            onClick = { /* TODO: Navigate to Settings screen */ }
        )
        // Add more options here later
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(newPadding),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(options) { option ->
            HomeOptionItem(
                option = option,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}