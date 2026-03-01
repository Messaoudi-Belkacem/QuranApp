# App Widgets

QuranApp exposes a single Android home-screen widget: the **Prayer Times Widget**. This document describes every file that participates in the widget implementation, how the pieces fit together, and the complete data flow from "user adds widget" to "prayer times appear on screen".

---

## Table of Contents

1. [Overview](#overview)
2. [Files Involved](#files-involved)
3. [Widget — `PrayerTimesWidget`](#widget--prayertimeswidget)
4. [Widget Receiver — `PrayerTimesWidgetReceiver`](#widget-receiver--prayertimeswidgetreceiver)
5. [Background Worker — `WidgetUpdateWorker`](#background-worker--widgetupdateworker)
6. [Widget Layout — `prayer_times_widget.xml`](#layout--prayer_times_widgetxml)
7. [Widget Metadata — `prayer_times_widget_info.xml`](#widget-metadata--prayer_times_widget_infoxml)
8. [Background Drawable — `widget_background.xml`](#background-drawable--widget_backgroundxml)
9. [Manifest Registration](#manifest-registration)
10. [Data Dependencies](#data-dependencies)
11. [End-to-End Data Flow](#end-to-end-data-flow)
12. [Permissions Required](#permissions-required)

---

## Overview

The Prayer Times Widget is a [Jetpack Glance](https://developer.android.com/jetpack/androidx/releases/glance) home-screen widget (`GlanceAppWidget`) that shows:

- **Next prayer** — name, scheduled time, and a countdown ("Xh Ym").
- **Last prayer** — name and elapsed time since it was due ("Xh Ym ago").

The widget is built with Glance's Compose-like DSL, which handles the `RemoteViews` translation automatically. It recalculates and redraws itself every 15 minutes through a `WorkManager` periodic job. Tapping anywhere on the widget opens `MainActivity`.

---

## Files Involved

| Role | Path |
|------|------|
| Glance widget | `app/src/main/java/com/example/quranapp/widget/PrayerTimesWidget.kt` |
| Widget receiver | `app/src/main/java/com/example/quranapp/widget/PrayerTimesWidgetReceiver.kt` |
| Periodic update worker | `app/src/main/java/com/example/quranapp/widget/WidgetUpdateWorker.kt` |
| Widget initial layout | `app/src/main/res/layout/prayer_times_widget.xml` |
| Widget metadata | `app/src/main/res/xml/prayer_times_widget_info.xml` |
| Background drawable | `app/src/main/res/drawable/widget_background.xml` |
| Manifest declaration | `app/src/main/AndroidManifest.xml` |

---

## Widget — `PrayerTimesWidget`

**File:** `app/src/main/java/com/example/quranapp/widget/PrayerTimesWidget.kt`

`PrayerTimesWidget` extends `GlanceAppWidget` and is the central class for the widget's data and UI.

### `provideGlance()`

The main entry point called by Glance whenever the widget needs to render. It:

1. **Reads the saved location** from `SharedPreferences` (key `"current_location"` in the `"quran_prefs"` store). If no location is present, the widget displays `"No Location"` / `"Enable GPS"` and skips prayer-time calculation.
2. **Loads prayer settings** from `PrayerSettingsRepository`: calculation method, Asr juristic method, and high-latitude rule.
3. **Computes today's prayer times** using `AdhanPrayerTimeCalculator` for the six canonical prayers: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha.
4. **Finds the next and last prayer** by linearly scanning the time-ordered list against `Date()` (now).
5. **Passes the computed data** to the Glance composable UI via `provideContent { }`.

### Composable UI

The widget UI is built using Glance's Compose-like DSL (`Column`, `Row`, `Box`, `Text`, `Image`, `Spacer`). The layout mirrors the original XML widget layout:

- **Header:** Mosque icon, "Prayer Times" label, update timestamp.
- **Next Prayer:** Prayer name (20sp), scheduled time (12-hour format), countdown ("Xh Ym" / "Now" / "Tomorrow").
- **Last Prayer:** Prayer name (16sp), elapsed time ("Xh Ym" / "Yesterday").
- **Tap action:** `actionStartActivity<MainActivity>()` opens the app on tap.

### Helper functions

- `calculateTimeDifference(from, to)` — Returns `"Xh Ym"`, `"Xm"`, or `"Now"`.
- `formatTime(date)` — Returns a 12-hour time string (`"hh:mm a"`).

---

## Widget Receiver — `PrayerTimesWidgetReceiver`

**File:** `app/src/main/java/com/example/quranapp/widget/PrayerTimesWidgetReceiver.kt`

`PrayerTimesWidgetReceiver` extends `GlanceAppWidgetReceiver` and bridges the Android widget system with the Glance widget.

| Method | When called | What it does |
|--------|-------------|--------------|
| `onEnabled()` | First widget instance is added to the launcher | Calls `WidgetUpdateWorker.schedulePeriodicUpdate()` to start the 15-minute refresh cycle. |
| `onDisabled()` | Last widget instance is removed from the launcher | Calls `WidgetUpdateWorker.cancelPeriodicUpdate()` to stop background work. |

---

## Background Worker — `WidgetUpdateWorker`

**File:** `app/src/main/java/com/example/quranapp/widget/WidgetUpdateWorker.kt`

`WidgetUpdateWorker` extends `androidx.work.CoroutineWorker` and is responsible for keeping the widget data fresh while the app is not in the foreground.

### `doWork()`

Calls `PrayerTimesWidget().updateAll(applicationContext)` which triggers Glance to re-run `provideGlance()` for every widget instance. Returns `Result.success()` on success or `Result.retry()` if an exception is thrown.

### `schedulePeriodicUpdate(context)`

Enqueues a unique periodic `WorkRequest`:

```kotlin
PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES)
    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(false).build())
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "widget_update_work",
    ExistingPeriodicWorkPolicy.KEEP,   // won't replace an already-running job
    updateRequest
)
```

- **Interval:** 15 minutes (the minimum allowed by WorkManager).
- **Policy:** `KEEP` — if the work is already scheduled it will not be re-queued.
- **Constraints:** none enforced (battery-not-low flag is explicitly set to `false`).

### `cancelPeriodicUpdate(context)`

Cancels the unique work by name `"widget_update_work"`. Called when the last widget instance is removed.

---

## Layout — `prayer_times_widget.xml`

**File:** `app/src/main/res/layout/prayer_times_widget.xml`

This XML layout serves as the **initial layout** displayed before Glance renders its composable content. Glance replaces it once `provideGlance()` completes.

### Structure

```
widget_container  (LinearLayout, vertical, id clickable)
│
├── Header row  (LinearLayout, horizontal)
│   ├── ic_mosque icon            (32 × 32 dp, tint #4CAF50)
│   ├── "Prayer Times" label      (16 sp, #FFFFFF, weight 1)
│   └── widget_update_time        (10 sp, #B3FFFFFF — "Updated: HH:mm")
│
├── Divider                       (1 dp, #FFFFFF @ 30 % opacity)
│
├── Next Prayer section           (LinearLayout, vertical)
│   ├── "NEXT PRAYER" label       (11 sp, #B3FFFFFF)
│   └── Row  (LinearLayout, horizontal)
│       ├── ic_prayer_time icon   (48 × 48 dp, tint #4CAF50)
│       ├── Column (weight 1)
│       │   ├── widget_next_prayer_name   (20 sp, #FFFFFF, medium)
│       │   └── widget_next_prayer_time  (14 sp, #B3FFFFFF)
│       └── Column (end-aligned)
│           ├── "in" label               (12 sp, #B3FFFFFF)
│           └── widget_time_remaining    (18 sp, #4CAF50, medium)
│
├── Divider                       (1 dp, #FFFFFF @ 20 % opacity)
│
└── Last Prayer section           (LinearLayout, vertical)
    ├── "LAST PRAYER" label       (11 sp, #B3FFFFFF)
    └── Row  (LinearLayout, horizontal)
        ├── ic_history icon       (40 × 40 dp, tint #808080)
        ├── Column (weight 1)
        │   └── widget_last_prayer_name   (16 sp, #E0E0E0, medium)
        └── Column (end-aligned)
            ├── "ago" label              (12 sp, #B3FFFFFF)
            └── widget_time_passed       (16 sp, #808080, medium)
```

### View IDs updated at runtime

| ID | Content |
|----|---------|
| `widget_update_time` | Last refresh timestamp |
| `widget_next_prayer_name` | Name of the upcoming prayer |
| `widget_next_prayer_time` | Scheduled time in 12-hour format |
| `widget_time_remaining` | Countdown string ("Xh Ym" / "Now" / "Tomorrow") |
| `widget_last_prayer_name` | Name of the most recent prayer |
| `widget_time_passed` | Elapsed time string ("Xh Ym" / "Yesterday") |

---

## Widget Metadata — `prayer_times_widget_info.xml`

**File:** `app/src/main/res/xml/prayer_times_widget_info.xml`

```xml
<appwidget-provider
    android:description="@string/widget_description"
    android:initialLayout="@layout/prayer_times_widget"
    android:minWidth="250dp"
    android:minHeight="200dp"
    android:previewImage="@drawable/ic_launcher_foreground"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="1800000"
    android:widgetCategory="home_screen" />
```

| Attribute | Value | Notes |
|-----------|-------|-------|
| `minWidth` | 250 dp | Minimum horizontal size in the launcher grid |
| `minHeight` | 200 dp | Minimum vertical size in the launcher grid |
| `updatePeriodMillis` | 1 800 000 ms (30 min) | System-level maximum refresh rate; the actual refresh is driven by the 15-minute `WorkManager` job |
| `resizeMode` | `horizontal\|vertical` | User can freely resize the widget |
| `widgetCategory` | `home_screen` | Widget only appears in the home-screen picker |

---

## Background Drawable — `widget_background.xml`

**File:** `app/src/main/res/drawable/widget_background.xml`

A rectangular `<shape>` with:

- **Gradient:** 135° linear, `#2C5F8D` → `#1E3A5F` (dark blue).
- **Corners:** 20 dp radius (rounded rectangle).
- **Stroke:** 1 dp, `#4A90E2` (lighter blue border).

---

## Manifest Registration

**File:** `app/src/main/AndroidManifest.xml`

```xml
<!-- Prayer Times Widget (Glance) -->
<receiver
    android:name=".widget.PrayerTimesWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/prayer_times_widget_info" />
</receiver>
```

- `android:exported="true"` is required so the Android launcher can send `APPWIDGET_UPDATE` broadcasts.
- The `<meta-data>` tag links the receiver to its configuration file.
- Glance handles widget updates internally — no custom broadcast action is needed.

---

## Data Dependencies

### `Location` — `data/model/Location.kt`

```kotlin
data class Location(val latitude: Float, val longitude: Float)
```

Stored in `SharedPreferences` as a comma-separated string (`"latitude,longitude"`). The widget reads it with key `"current_location"` from the `"quran_prefs"` preference file.

### `PrayerSettingsRepository` — `data/repository/PrayerSettingsRepository.kt`

Provides three settings consumed by the widget:

| Method | Purpose |
|--------|---------|
| `getCalculationMethod()` | Prayer-time formula (ISNA, MWL, etc.) |
| `getAsrMethod()` | Asr shadow ratio (Shafii default or Hanafi) |
| `getHighLatitudeMethod()` | Rule for high-latitude locations |

### `AdhanPrayerTimeCalculator` — `util/AdhanPrayerTimeCalculator.kt`

Wraps the [Adhan2 library by Batoul Apps](https://github.com/batoulapps/adhan-java), which implements Jean Meeus' *Astronomical Algorithms* for precise Islamic prayer-time calculation. Given latitude, longitude, and the three settings above, `getPrayerTimes(date)` returns a `Map<String, Date>` for Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha.

---

## End-to-End Data Flow

```
1. User adds widget to home screen
        │
        ▼
2. PrayerTimesWidgetReceiver.onEnabled()
        │  Calls WidgetUpdateWorker.schedulePeriodicUpdate()
        ▼
3. WorkManager enqueues "widget_update_work" (every 15 min)
        │
        ▼
4. WidgetUpdateWorker.doWork()  [runs every 15 min]
        │  Calls PrayerTimesWidget().updateAll(context)
        ▼
5. Glance triggers PrayerTimesWidget.provideGlance()
        │
        ▼
6. provideGlance()
        ├─ Read location from SharedPreferences ("quran_prefs")
        ├─ Load prayer settings from PrayerSettingsRepository
        ├─ Compute prayer times with AdhanPrayerTimeCalculator
        ├─ Identify next prayer (first prayer after now)
        ├─ Identify last prayer (last prayer before now)
        ├─ Format countdown / elapsed strings
        └─ provideContent { } renders Glance composable UI
                │
                ▼
           Glance translates composables → RemoteViews → widget redraws

7. User taps widget → actionStartActivity<MainActivity>() opens app

8. User removes last widget
        │
        ▼
9. PrayerTimesWidgetReceiver.onDisabled()
        │  Calls WidgetUpdateWorker.cancelPeriodicUpdate()
        ▼
10. WorkManager cancels "widget_update_work"
```

---

## Permissions Required

| Permission | Reason |
|------------|--------|
| `ACCESS_FINE_LOCATION` | Precise GPS coordinates for prayer-time calculation |
| `ACCESS_COARSE_LOCATION` | Fallback network-based location |

WorkManager itself does not require additional permissions for background execution on Android 12+.
