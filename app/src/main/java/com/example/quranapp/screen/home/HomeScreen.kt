package com.example.quranapp.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    innerPadding: PaddingValues
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Home", modifier = Modifier.align(Alignment.Center))
    }
}