Below is a **complete, Play-Store-safe, Jetpack Compose–oriented implementation**, designed specifically for a **prayer countdown foreground notification** that:

* Works correctly with **Jetpack Compose**
* Is **battery-aware** (Doze-friendly)
* **Restarts after reboot**
* Complies with **modern Android + Play Store policies**

I will be precise and conservative (this matters for Play Store approval).

---

## 1. High-level architecture (recommended)

**Do NOT tie this to UI state directly.**

**Correct layers**

* Prayer time calculation → domain layer (pure Kotlin)
* ForegroundService → notification + lifecycle
* Compose UI → only starts/stops service
* BootReceiver → restarts service after reboot
* Optional WorkManager → recalculation, not notification

---

## 2. Manifest (Play Store compliant)

### Permissions

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Android 13+ -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Service

```xml
<service
    android:name=".prayer.PrayerForegroundService"
    android:exported="false"
    android:foregroundServiceType="specialUse" />
```

### Boot receiver

```xml
<receiver
    android:name=".prayer.BootReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

---

## 3. Foreground Service (Doze-optimized)

Key rules:

* **No high-frequency updates**
* **1-minute updates max**
* **No wake locks**
* **IMPORTANCE_LOW notification**

```kotlin
class PrayerForegroundService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(ID, buildNotification("Calculating next prayer…"))
        scheduleUpdates()
    }

    private fun scheduleUpdates() {
        runnable = Runnable {
            val next = PrayerTimesRepo.getNextPrayer()
            val remaining = PrayerTimesRepo.getRemainingTime()

            updateNotification(
                "Next prayer: ${next.name} in $remaining"
            )

            handler.postDelayed(runnable, 60_000L)
        }
        handler.post(runnable)
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle("Prayer Time")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL,
                "Prayer Time",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(i: Intent?, f: Int, s: Int) = START_STICKY
    override fun onBind(i: Intent?) = null

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    companion object {
        private const val ID = 1001
        private const val CHANNEL = "prayer_channel"
    }
}
```

---

## 4. Jetpack Compose integration (clean)

Compose **does NOT manage the service** directly.
It only **requests start/stop**.

```kotlin
@Composable
fun PrayerNotificationToggle() {
    val context = LocalContext.current
    var enabled by rememberSaveable { mutableStateOf(false) }

    Switch(
        checked = enabled,
        onCheckedChange = {
            enabled = it
            val intent = Intent(context, PrayerForegroundService::class.java)
            if (it) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.stopService(intent)
            }
        }
    )
}
```

✔ Play Store expects a **user-controlled toggle**.

---

## 5. Restart after reboot (mandatory)

```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (UserPrefs.isPrayerNotificationEnabled(context)) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, PrayerForegroundService::class.java)
                )
            }
        }
    }
}
```

✔ This is allowed because:

* User explicitly enabled it
* Notification is visible
* Service has a legitimate user-facing purpose

---

## 6. Doze & battery optimization (critical)

### What you MUST do

* Update **once per minute**
* Use `IMPORTANCE_LOW`
* Avoid `WakeLock`
* Avoid `AlarmManager` for UI updates
* Do not bypass battery optimizations silently

### Optional (recommended)

Near prayer time (≤10 min), update every 30 seconds:

```kotlin
val delay = if (remainingMinutes <= 10) 30_000L else 60_000L
handler.postDelayed(runnable, delay)
```

---

## 7. Play Store compliance checklist

Google reviewers check this:

✔ Foreground service has **clear user value**
✔ Notification explains why it’s persistent
✔ User can **disable it**
✔ No misleading background behavior
✔ No excessive battery usage
✔ No hidden wake locks

**Settings text example (important):**

> “This notification stays visible to continuously show the remaining time until the next prayer.”

---

## 8. What NOT to do (common rejection causes)

* ❌ Using WorkManager for persistent UI notification
* ❌ Updating every second
* ❌ Hiding the notification
* ❌ Auto-starting without user consent
* ❌ Claiming “system requirement” falsely

---

## 9. Final summary

**This solution is:**

* Jetpack Compose–friendly
* Doze-aware
* Reboot-safe
* Battery-efficient
* Fully Play Store compliant

## Implement this carefully to ensure smooth approval! use the existing prayer times implementation in your app for the calculation logic specifically the Adhan library.