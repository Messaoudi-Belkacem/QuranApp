package com.example.quranapp.presentation.screen.home

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.quranapp.data.repository.PrayerSettingsRepository
import com.example.quranapp.service.PrayerForegroundService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Prayer Notification Toggle Component
 *
 * Allows users to enable/disable the persistent prayer countdown notification.
 * Handles notification permissions for Android 13+.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerNotificationToggle(
    prayerSettingsRepository: PrayerSettingsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEnabled by remember {
        mutableStateOf(prayerSettingsRepository.isPrayerNotificationEnabled())
    }
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Android 13+ notification permission
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Prayer Countdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Show time until next prayer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { shouldEnable ->
                        // Check permission for Android 13+
                        if (shouldEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val permissionState = notificationPermissionState
                            if (permissionState != null && !permissionState.status.isGranted) {
                                if (permissionState.status.shouldShowRationale) {
                                    showPermissionRationale = true
                                } else {
                                    permissionState.launchPermissionRequest()
                                }
                                return@Switch
                            }
                        }

                        // Toggle service
                        val serviceIntent = Intent(context, PrayerForegroundService::class.java)

                        if (shouldEnable) {
                            // Start service
                            ContextCompat.startForegroundService(context, serviceIntent)
                            prayerSettingsRepository.setPrayerNotificationEnabled(true)
                            isEnabled = true
                        } else {
                            // Stop service
                            context.stopService(serviceIntent)
                            prayerSettingsRepository.setPrayerNotificationEnabled(false)
                            isEnabled = false
                        }
                    }
                )
            }

            // Description/Info text
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This notification stays visible to continuously show the remaining time until the next prayer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 36.dp)
            )
        }
    }

    // Permission rationale dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Notification Permission Required") },
            text = {
                Text("To show prayer countdown notifications, please grant notification permission.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        notificationPermissionState?.launchPermissionRequest()
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

