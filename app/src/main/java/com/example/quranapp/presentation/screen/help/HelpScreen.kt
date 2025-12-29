package com.example.quranapp.presentation.screen.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                HelpItem(
                    question = "How do I find the Qibla direction?",
                    answer = "Navigate to the Qibla tab from the bottom navigation bar. The compass will automatically point towards the Qibla direction. Make sure to allow location permissions for accurate results."
                )
            }

            item {
                HelpItem(
                    question = "How do prayer times work?",
                    answer = "Prayer times are calculated based on your current location. Ensure location permissions are granted for accurate prayer time calculations."
                )
            }

            item {
                HelpItem(
                    question = "How do I use the Tasbih counter?",
                    answer = "Go to the Tasbih screen from the More menu. Tap the center to increment the count. You can reset the counter using the reset button."
                )
            }

            item {
                HelpItem(
                    question = "How do I read the Quran?",
                    answer = "Navigate to the Quran tab, browse through the list of Surahs, and tap on any Surah to start reading. You can search for specific Ayahs within each Surah."
                )
            }

            item {
                HelpItem(
                    question = "What are Adhkar?",
                    answer = "Adhkar are remembrances and supplications. Access them from the More menu to view morning and evening dhikr."
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Need More Help?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If you have additional questions or need support, please contact us through the About section.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HelpItem(
    question: String,
    answer: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

