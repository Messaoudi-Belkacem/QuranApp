package com.example.quranapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.quranapp.data.repository.PrayerSettingsRepository
import com.example.quranapp.service.PrayerForegroundService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Broadcast receiver that restarts the prayer notification service after device reboot.
 *
 * This only starts the service if the user previously enabled it.
 * This is compliant with Play Store policies because:
 * - User explicitly enabled the feature
 * - Notification is visible and provides clear value
 * - Service has legitimate user-facing purpose
 */
@AndroidEntryPoint
class PrayerServiceBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var prayerSettingsRepository: PrayerSettingsRepository

    companion object {
        private const val TAG = "PrayerBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, checking if prayer notification was enabled")

            try {
                // Check if user had enabled prayer notification before reboot
                if (prayerSettingsRepository.isPrayerNotificationEnabled()) {
                    Log.d(TAG, "Prayer notification was enabled, restarting service")

                    val serviceIntent = Intent(context, PrayerForegroundService::class.java)
                    ContextCompat.startForegroundService(context, serviceIntent)

                    Log.d(TAG, "Prayer notification service started successfully")
                } else {
                    Log.d(TAG, "Prayer notification was not enabled, skipping")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting prayer notification service after boot", e)
            }
        }
    }
}

