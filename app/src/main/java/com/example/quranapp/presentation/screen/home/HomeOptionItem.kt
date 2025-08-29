package com.example.quranapp.presentation.screen.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeOptionItem(
    option: HomeOption,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { option.onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.Companion
                .size(64.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Companion.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                modifier = Modifier.Companion.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.Companion.height(8.dp))

        Text(
            text = option.title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Companion.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}