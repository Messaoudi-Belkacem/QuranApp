package com.example.quranapp.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDeniedDialog(onDismiss: () -> Unit) {
    val activity = LocalContext.current as Activity
    BasicAlertDialog(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp)),
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Permission Denied",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "You have denied the permission to access storage." +
                        " Unfortunately, you cannot use this app without granting this permission." +
                        " Please enable storage access in your device settings to continue using the app.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextButton(
                    onClick = { activity.finish() }
                ) {
                    Text(
                        text = "Leave the app",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}