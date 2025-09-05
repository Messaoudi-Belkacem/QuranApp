package com.example.quranapp.presentation.screen.quran

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quranapp.R

@Composable
fun QuranHeader(
    surahName: String = "Al-Fatihah",
    ayahNumber: Int = 1
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Column(
                modifier = Modifier.Companion.weight(1f),
                horizontalAlignment = Alignment.Companion.Start
            ) {
                Text(
                    text = "Last Read",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Companion.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = surahName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Companion.Medium
                )

                Text(
                    text = "Ayah $ayahNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.mosque_image),
                contentDescription = "Quran Icon",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}