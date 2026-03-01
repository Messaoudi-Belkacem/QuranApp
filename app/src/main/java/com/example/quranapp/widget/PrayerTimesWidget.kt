package com.example.quranapp.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.quranapp.MainActivity
import com.example.quranapp.R
import com.example.quranapp.data.model.Location
import com.example.quranapp.data.repository.PrayerSettingsRepository
import com.example.quranapp.util.AdhanPrayerTimeCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Prayer Times Widget built with Jetpack Glance.
 *
 * Shows the next upcoming prayer (name, scheduled time, countdown) and the
 * last prayer (name, elapsed time). Tapping anywhere on the widget opens
 * [MainActivity].
 *
 * Prayer-time calculation reuses [AdhanPrayerTimeCalculator],
 * [PrayerSettingsRepository] and [Location] — the same classes used by
 * the main Adhan / Prayer-Times screen.
 */
class PrayerTimesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = computePrayerData(context)

        provideContent {
            GlanceTheme {
                PrayerTimesContent(data)
            }
        }
    }

    // ── Data model ──────────────────────────────────────────────────

    private data class PrayerWidgetData(
        val nextPrayerName: String,
        val nextPrayerTime: String,
        val timeRemaining: String,
        val lastPrayerName: String,
        val timePassed: String,
        val updateTime: String,
        val hasLocation: Boolean
    )

    // ── Compute prayer data (runs inside provideGlance suspend) ─────

    private fun computePrayerData(context: Context): PrayerWidgetData {
        val prefs = context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)
        val locationString = prefs.getString("current_location", null)
        val location = locationString?.let { Location.fromString(it) }

        val now = Date()
        val updateTime = "Updated: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)}"

        if (location == null) {
            return PrayerWidgetData(
                nextPrayerName = "No Location",
                nextPrayerTime = "--:--",
                timeRemaining = "Enable GPS",
                lastPrayerName = "",
                timePassed = "",
                updateTime = updateTime,
                hasLocation = false
            )
        }

        val prayerSettingsRepository = PrayerSettingsRepository(context)
        val calculator = AdhanPrayerTimeCalculator(
            latitude = location.latitude.toDouble(),
            longitude = location.longitude.toDouble(),
            calculationMethod = prayerSettingsRepository.getCalculationMethod(),
            asrMethod = prayerSettingsRepository.getAsrMethod(),
            highLatMethod = prayerSettingsRepository.getHighLatitudeMethod()
        )

        val prayerTimesMap = calculator.getPrayerTimes(now)
        val prayers = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
            .mapNotNull { name -> prayerTimesMap[name]?.let { name to it } }

        var nextPrayer: Pair<String, Date>? = null
        var lastPrayer: Pair<String, Date>? = null

        for ((name, time) in prayers) {
            if (time.after(now) && nextPrayer == null) nextPrayer = name to time
            if (time.before(now)) lastPrayer = name to time
        }

        return PrayerWidgetData(
            nextPrayerName = nextPrayer?.first ?: "Fajr",
            nextPrayerTime = nextPrayer?.let { formatTime(it.second) } ?: "--:--",
            timeRemaining = nextPrayer?.let { calculateTimeDifference(now, it.second) } ?: "Tomorrow",
            lastPrayerName = lastPrayer?.first ?: "Isha",
            timePassed = lastPrayer?.let { calculateTimeDifference(it.second, now) } ?: "Yesterday",
            updateTime = updateTime,
            hasLocation = true
        )
    }

    // ── Helpers (same logic used by the original widget) ────────────

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

    private fun formatTime(date: Date): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)

    // ── Glance Composable UI ────────────────────────────────────────

    @androidx.compose.runtime.Composable
    private fun PrayerTimesContent(data: PrayerWidgetData) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_background))
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                HeaderRow(data.updateTime)
                Divider(alpha30 = true)
                NextPrayerSection(data)
                Divider(alpha30 = false)
                if (data.hasLocation) {
                    LastPrayerSection(data)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun HeaderRow(updateTime: String) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_mosque),
                contentDescription = "Mosque",
                modifier = GlanceModifier.size(32.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "Prayer Times",
                style = TextStyle(
                    color = ColorProvider(WHITE, WHITE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = updateTime,
                style = TextStyle(
                    color = ColorProvider(MUTED_WHITE, MUTED_WHITE),
                    fontSize = 10.sp
                )
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun Divider(alpha30: Boolean) {
        val color = if (alpha30) DIVIDER_30 else DIVIDER_20
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ColorProvider(color, color))
        ) {}
    }

    @androidx.compose.runtime.Composable
    private fun NextPrayerSection(data: PrayerWidgetData) {
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = "NEXT PRAYER",
            style = TextStyle(
                color = ColorProvider(MUTED_WHITE, MUTED_WHITE),
                fontSize = 11.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_prayer_time),
                contentDescription = "Next Prayer",
                modifier = GlanceModifier.size(48.dp)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = data.nextPrayerName,
                    style = TextStyle(
                        color = ColorProvider(WHITE, WHITE),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = data.nextPrayerTime,
                    style = TextStyle(
                        color = ColorProvider(MUTED_WHITE, MUTED_WHITE),
                        fontSize = 14.sp
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "in",
                    style = TextStyle(
                        color = ColorProvider(MUTED_WHITE, MUTED_WHITE),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = data.timeRemaining,
                    style = TextStyle(
                        color = ColorProvider(ACCENT_GREEN, ACCENT_GREEN),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        Spacer(modifier = GlanceModifier.height(12.dp))
    }

    @androidx.compose.runtime.Composable
    private fun LastPrayerSection(data: PrayerWidgetData) {
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = "LAST PRAYER",
            style = TextStyle(
                color = ColorProvider(MUTED_WHITE, MUTED_WHITE),
                fontSize = 11.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_history),
                contentDescription = "Last Prayer",
                modifier = GlanceModifier.size(40.dp)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Text(
                text = data.lastPrayerName,
                style = TextStyle(
                    color = ColorProvider(LIGHT_GRAY, LIGHT_GRAY),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "ago",
                    style = TextStyle(
                        color = ColorProvider(MUTED_WHITE, MUTED_WHITE),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = data.timePassed,
                    style = TextStyle(
                        color = ColorProvider(GRAY, GRAY),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }

    companion object {
        private val WHITE = Color(0xFFFFFFFF)
        private val MUTED_WHITE = Color(0xB3FFFFFF)
        private val ACCENT_GREEN = Color(0xFF4CAF50)
        private val LIGHT_GRAY = Color(0xFFE0E0E0)
        private val GRAY = Color(0xFF808080)
        private val DIVIDER_30 = Color(0x4DFFFFFF)
        private val DIVIDER_20 = Color(0x33FFFFFF)
    }
}
