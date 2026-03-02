package com.example.quranapp.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker to periodically update the prayer times widget.
 *
 * Instead of sending a broadcast (the old AppWidgetProvider approach),
 * this now calls [PrayerTimesWidget.updateAll] which triggers Glance
 * to re-run [PrayerTimesWidget.provideGlance] for every widget instance.
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            PrayerTimesWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "widget_update_work"

        /**
         * Schedule periodic widget updates every 15 minutes
         */
        fun schedulePeriodicUpdate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest
            )
        }

        /**
         * Cancel scheduled widget updates
         */
        fun cancelPeriodicUpdate(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
