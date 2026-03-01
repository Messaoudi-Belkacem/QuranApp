package com.example.quranapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver that bridges the Android widget system with the Glance-based
 * [PrayerTimesWidget]. Handles scheduling / cancelling the periodic
 * [WidgetUpdateWorker] when widgets are added or removed.
 */
class PrayerTimesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = PrayerTimesWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.schedulePeriodicUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateWorker.cancelPeriodicUpdate(context)
    }
}
