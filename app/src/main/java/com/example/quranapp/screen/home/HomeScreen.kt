package com.example.quranapp.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quranapp.util.addPaddingValues

@Composable
fun HomeScreen(
    innerPadding: PaddingValues
) {
    val newPadding = addPaddingValues(innerPadding, PaddingValues(24.dp))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(newPadding)
    ) {
        Text(text = "Home", modifier = Modifier.align(Alignment.Center))
    }
}