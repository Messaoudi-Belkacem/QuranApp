package com.example.quranapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.quranapp.MainActivity
import com.example.quranapp.R
import com.example.quranapp.data.repository.PrayerSettingsRepository
import com.example.quranapp.data.repository.QuranRepository
import com.example.quranapp.util.AdhanPrayerTimeCalculator
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Foreground service that displays a persistent notification showing
 * countdown to the next prayer time.
 *
 * This implementation is:
 * - Play Store compliant
 * - Battery efficient (updates every 1 minute, or 30 seconds when < 10 minutes remaining)
 * - Doze-friendly (no wake locks, no AlarmManager for UI)
 * - User-controlled (can be started/stopped by user)
 */
@AndroidEntryPoint
class PrayerForegroundService : Service() {

    @Inject
    lateinit var prayerSettingsRepository: PrayerSettingsRepository

    @Inject
    lateinit var quranRepository: QuranRepository

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable
    private var isRunning = false

    companion object {
        private const val TAG = "PrayerForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "prayer_countdown_channel"
        private const val CHANNEL_NAME = "Prayer Time Countdown"

        // Update intervals
        private const val UPDATE_INTERVAL_NORMAL = 60_000L // 1 minute
        private const val UPDATE_INTERVAL_NEAR = 30_000L // 30 seconds when < 10 minutes
        private const val NEAR_PRAYER_THRESHOLD_MINUTES = 10
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Start foreground with initial notification
        startForeground(NOTIFICATION_ID, buildNotification("Calculating next prayer..."))

        // Start periodic updates
        if (!isRunning) {
            isRunning = true
            scheduleNextUpdate()
        }

        return START_STICKY // Restart service if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }

    private fun scheduleNextUpdate() {
        updateRunnable = Runnable {
            if (isRunning) {
                updateNotification()
                scheduleNextUpdate() // Schedule next update
            }
        }

        // Calculate update interval based on remaining time
        val updateInterval = calculateUpdateInterval()
        handler.postDelayed(updateRunnable, updateInterval)
    }

    private fun calculateUpdateInterval(): Long {
        val nextPrayer = getNextPrayerInfo()
        val remainingMinutes = nextPrayer.second / 60_000 // Convert ms to minutes

        return if (remainingMinutes <= NEAR_PRAYER_THRESHOLD_MINUTES) {
            UPDATE_INTERVAL_NEAR
        } else {
            UPDATE_INTERVAL_NORMAL
        }
    }

    private fun updateNotification() {
        try {
            val nextPrayer = getNextPrayerInfo()
            val prayerName = nextPrayer.first
            val remainingTime = formatRemainingTime(nextPrayer.second)

            val text = if (prayerName.isNotEmpty()) {
                "Next prayer: $prayerName in $remainingTime"
            } else {
                "Calculating next prayer..."
            }

            val notification = buildNotification(text)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

            Log.d(TAG, "Notification updated: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification", e)
        }
    }

    /**
     * Get next prayer info
     * @return Pair of (prayer name, remaining time in milliseconds)
     */
    private fun getNextPrayerInfo(): Pair<String, Long> {
        try {
            // Get current location
            val location = quranRepository.getCurrentLocation() ?: return Pair("", 0L)

            // Get prayer calculation settings
            val calculationMethod = prayerSettingsRepository.getCalculationMethod()
            val asrMethod = prayerSettingsRepository.getAsrMethod()
            val highLatMethod = prayerSettingsRepository.getHighLatitudeMethod()

            // Create calculator
            val calculator = AdhanPrayerTimeCalculator(
                latitude = location.latitude.toDouble(),
                longitude = location.longitude.toDouble(),
                calculationMethod = calculationMethod,
                asrMethod = asrMethod,
                highLatMethod = highLatMethod
            )

            // Get prayer times for today
            val now = Date()
            val prayerTimesMap = calculator.getPrayerTimes(now)

            // Find next prayer
            val prayerOrder = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            val currentTime = now.time

            for (prayerName in prayerOrder) {
                val prayerTime = prayerTimesMap[prayerName]
                if (prayerTime != null && prayerTime.time > currentTime) {
                    val remainingMs = prayerTime.time - currentTime
                    return Pair(prayerName, remainingMs)
                }
            }

            // If no prayer found today, get Fajr from tomorrow
            val tomorrow = Calendar.getInstance().apply {
                time = now
                add(Calendar.DAY_OF_MONTH, 1)
            }.time

            val tomorrowPrayers = calculator.getPrayerTimes(tomorrow)
            val fajrTomorrow = tomorrowPrayers["Fajr"]

            if (fajrTomorrow != null) {
                val remainingMs = fajrTomorrow.time - currentTime
                return Pair("Fajr", remainingMs)
            }

            return Pair("", 0L)

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating next prayer", e)
            return Pair("", 0L)
        }
    }

    /**
     * Format remaining time in human-readable format
     */
    private fun formatRemainingTime(milliseconds: Long): String {
        if (milliseconds <= 0) return "0m"

        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    private fun buildNotification(text: String): Notification {
        // Create intent to open app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle("Prayer Time Countdown")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true) // Don't make sound/vibration on updates
            .setOngoing(true) // Cannot be dismissed by user
            .setPriority(NotificationCompat.PRIORITY_LOW) // Battery-friendly
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance = no sound/vibration
            ).apply {
                description = "This notification stays visible to continuously show the remaining time until the next prayer."
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created")
        }
    }
}

