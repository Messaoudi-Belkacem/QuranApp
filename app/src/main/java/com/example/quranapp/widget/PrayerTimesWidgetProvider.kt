package com.example.quranapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.quranapp.MainActivity
import com.example.quranapp.R
import com.example.quranapp.data.repository.PrayerSettingsRepository
import com.example.quranapp.data.model.Location
import com.example.quranapp.util.AdhanPrayerTimeCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Prayer Times Widget Provider
 * Shows time remaining to next prayer and time passed since last prayer
 */
class PrayerTimesWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Schedule periodic updates every 15 minutes
        WidgetUpdateWorker.schedulePeriodicUpdate(context)
    }

    override fun onDisabled(context: Context) {
        // Cancel periodic updates when last widget is removed
        WidgetUpdateWorker.cancelPeriodicUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, PrayerTimesWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.quranapp.ACTION_UPDATE_WIDGET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val views = RemoteViews(context.packageName, R.layout.prayer_times_widget)

                    // Get location from SharedPreferences
                    val prefs = context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)
                    val locationString = prefs.getString("current_location", null)
                    val location = locationString?.let { Location.fromString(it) }

                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // Get prayer calculation settings
                        val prayerSettingsRepository = PrayerSettingsRepository(context)
                        val calculationMethod = prayerSettingsRepository.getCalculationMethod()
                        val asrMethod = prayerSettingsRepository.getAsrMethod()
                        val highLatMethod = prayerSettingsRepository.getHighLatitudeMethod()

                        // Calculate prayer times
                        val calculator = AdhanPrayerTimeCalculator(
                            latitude = latitude.toDouble(),
                            longitude = longitude.toDouble(),
                            calculationMethod = calculationMethod,
                            asrMethod = asrMethod,
                            highLatMethod = highLatMethod
                        )

                        val now = Date()
                        val prayerTimesMap = calculator.getPrayerTimes(now)

                        // Create sorted list of prayers
                        val prayers = listOf(
                            "Fajr" to prayerTimesMap["Fajr"],
                            "Sunrise" to prayerTimesMap["Sunrise"],
                            "Dhuhr" to prayerTimesMap["Dhuhr"],
                            "Asr" to prayerTimesMap["Asr"],
                            "Maghrib" to prayerTimesMap["Maghrib"],
                            "Isha" to prayerTimesMap["Isha"]
                        ).filter { it.second != null }

                        // Find next and last prayers
                        var nextPrayer: Pair<String, Date>? = null
                        var lastPrayer: Pair<String, Date>? = null

                        for (prayer in prayers) {
                            val prayerTime = prayer.second!!
                            if (prayerTime.after(now) && nextPrayer == null) {
                                nextPrayer = prayer.first to prayerTime
                            }
                            if (prayerTime.before(now)) {
                                lastPrayer = prayer.first to prayerTime
                            }
                        }

                        // Calculate time differences
                        if (nextPrayer != null) {
                            val timeToNext = calculateTimeDifference(now, nextPrayer.second)
                            views.setTextViewText(R.id.widget_next_prayer_name, nextPrayer.first)
                            views.setTextViewText(R.id.widget_next_prayer_time, formatTime(nextPrayer.second))
                            views.setTextViewText(R.id.widget_time_remaining, timeToNext)
                        } else {
                            views.setTextViewText(R.id.widget_next_prayer_name, "Fajr")
                            views.setTextViewText(R.id.widget_next_prayer_time, "--:--")
                            views.setTextViewText(R.id.widget_time_remaining, "Tomorrow")
                        }

                        if (lastPrayer != null) {
                            val timeSinceLast = calculateTimeDifference(lastPrayer.second, now)
                            views.setTextViewText(R.id.widget_last_prayer_name, lastPrayer.first)
                            views.setTextViewText(R.id.widget_time_passed, timeSinceLast)
                        } else {
                            views.setTextViewText(R.id.widget_last_prayer_name, "Isha")
                            views.setTextViewText(R.id.widget_time_passed, "Yesterday")
                        }

                        // Update timestamp
                        views.setTextViewText(
                            R.id.widget_update_time,
                            "Updated: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)}"
                        )
                    } else {
                        // No location available
                        views.setTextViewText(R.id.widget_next_prayer_name, "No Location")
                        views.setTextViewText(R.id.widget_next_prayer_time, "--:--")
                        views.setTextViewText(R.id.widget_time_remaining, "Enable GPS")
                        views.setTextViewText(R.id.widget_last_prayer_name, "")
                        views.setTextViewText(R.id.widget_time_passed, "")
                    }

                    // Set up click to open app
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                    // Instruct the widget manager to update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun calculateTimeDifference(from: Date, to: Date): String {
            val diff = abs(to.time - from.time)
            val hours = (diff / (1000 * 60 * 60)).toInt()
            val minutes = ((diff / (1000 * 60)) % 60).toInt()

            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "Now"
            }
        }

        private fun formatTime(date: Date): String {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return format.format(date)
        }

        fun requestWidgetUpdate(context: Context) {
            val intent = Intent(context, PrayerTimesWidgetProvider::class.java)
            intent.action = ACTION_UPDATE_WIDGET
            context.sendBroadcast(intent)
        }
    }
}

