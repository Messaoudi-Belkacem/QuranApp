# Prayer Countdown Notification Implementation

## Overview
This implementation adds a **persistent foreground notification** that displays a live countdown to the next prayer time. The feature is:

- ✅ **Play Store compliant**
- ✅ **Battery efficient** (Doze-friendly)
- ✅ **Reboot-persistent** (restarts after device reboot if enabled by user)
- ✅ **User-controlled** (can be toggled on/off)
- ✅ **Permission-aware** (handles Android 13+ notification permissions)

---

## Architecture

### Components Created/Modified

1. **PrayerForegroundService.kt** (`service/`)
   - Foreground service that displays the countdown notification
   - Updates every 1 minute (or 30 seconds when < 10 minutes remain)
   - Uses existing `AdhanPrayerTimeCalculator` for prayer time calculations
   - Low priority notification (no sound/vibration)

2. **PrayerServiceBootReceiver.kt** (`receiver/`)
   - BroadcastReceiver that restarts the service after device reboot
   - Only restarts if user previously enabled the feature
   - Fully compliant with Play Store policies

3. **PrayerNotificationToggle.kt** (`presentation/screen/home/`)
   - Compose UI component with a switch to enable/disable notification
   - Handles Android 13+ POST_NOTIFICATIONS permission
   - Provides clear explanation of the feature

4. **PrayerSettingsRepository.kt** (modified)
   - Added methods to persist notification enabled state:
     - `isPrayerNotificationEnabled()`
     - `setPrayerNotificationEnabled()`

5. **PrayerSettingsDialog.kt** (modified)
   - Added 4th tab "Notif" with the notification toggle

6. **AndroidManifest.xml** (modified)
   - Added required permissions
   - Registered foreground service with `specialUse` type
   - Registered boot receiver

7. **build.gradle.kts & libs.versions.toml** (modified)
   - Added accompanist-permissions library for permission handling

---

## How It Works

### User Workflow

1. User opens Prayer Settings (gear icon on home screen)
2. User navigates to "Notif" tab
3. User toggles "Prayer Countdown" switch
4. On Android 13+, permission dialog appears (if not already granted)
5. Service starts and notification appears
6. Notification updates automatically every minute
7. Service persists across app restarts and device reboots

### Technical Flow

```
User Toggle ON
    ↓
Check POST_NOTIFICATIONS permission (Android 13+)
    ↓
Start PrayerForegroundService
    ↓
Service creates notification channel (IMPORTANCE_LOW)
    ↓
Service displays initial notification
    ↓
Schedule periodic updates (Handler with 60s delay)
    ↓
Every update:
    - Get current location from QuranRepository
    - Get prayer settings from PrayerSettingsRepository
    - Create AdhanPrayerTimeCalculator
    - Calculate next prayer time
    - Format remaining time (Xh Ym)
    - Update notification
    ↓
When < 10 minutes remain: Update every 30 seconds
```

---

## Battery Optimization

### Doze Compliance
- ✅ Updates only once per minute (not per second)
- ✅ Uses Handler (not AlarmManager or WorkManager)
- ✅ No wake locks
- ✅ IMPORTANCE_LOW notification (no sound/vibration)
- ✅ Service stops when user toggles off

### Update Strategy
- **Normal**: Update every 60 seconds
- **Near Prayer** (≤10 min): Update every 30 seconds
- This provides a good balance between accuracy and battery life

---

## Play Store Compliance

### What Makes This Compliant

1. **User Control**: Feature is explicitly enabled by user via toggle
2. **Clear Purpose**: Notification clearly states "Prayer Time Countdown"
3. **Transparent Behavior**: Description explains why notification is persistent
4. **Legitimate Use**: Prayer countdown is a valid user-facing feature
5. **Visible Notification**: Always visible when service is running
6. **foregroundServiceType**: Set to `specialUse` with clear description
7. **Boot Restart**: Only restarts if user previously enabled it

### Notification Channel Configuration
```kotlin
NotificationChannel(
    id = "prayer_countdown_channel",
    name = "Prayer Time Countdown",
    importance = NotificationManager.IMPORTANCE_LOW
)
```

- **IMPORTANCE_LOW**: No sound, no vibration, no pop-up
- **setShowBadge(false)**: Doesn't show notification badge
- **setOngoing(true)**: Cannot be dismissed (indicates active service)

---

## Permissions

### Required Permissions (AndroidManifest.xml)

```xml
<!-- Core service permissions -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Android 13+ notification permission -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Runtime Permission Handling
The app requests `POST_NOTIFICATIONS` permission only when user tries to enable the feature (Android 13+). This is handled by the `PrayerNotificationToggle` component using Accompanist Permissions library.

---

## Integration Points

### Uses Existing Components
- `AdhanPrayerTimeCalculator` - For accurate prayer time calculations
- `QuranRepository` - For accessing stored location
- `PrayerSettingsRepository` - For prayer calculation settings
- Adhan library (`com.batoulapps.adhan`) - For prayer time calculations

### Shared with Other Features
- Location data (shared with prayer times display and widget)
- Prayer calculation settings (shared with home screen)
- Calculation method preferences (MWL, Egyptian, etc.)

---

## Testing Checklist

### Basic Functionality
- [ ] Toggle switch enables/disables notification
- [ ] Notification appears immediately when enabled
- [ ] Notification shows correct next prayer name
- [ ] Countdown updates every minute
- [ ] Countdown accelerates to 30s when < 10 min remain
- [ ] Service stops when toggle is switched off

### Permission Handling
- [ ] On Android 13+, permission dialog appears
- [ ] Permission rationale shows if user denies once
- [ ] Service starts after permission granted
- [ ] Service doesn't start if permission denied

### Reboot Persistence
- [ ] Enable notification, then reboot device
- [ ] Notification reappears after reboot
- [ ] Disable notification, then reboot device
- [ ] Notification does NOT appear after reboot

### Prayer Time Transitions
- [ ] When prayer time passes, notification updates to next prayer
- [ ] Countdown resets appropriately
- [ ] Handles midnight transition (switches to Fajr next day)

### Battery Behavior
- [ ] Service continues in Doze mode (Android 6+)
- [ ] Updates don't cause excessive battery drain
- [ ] Service doesn't prevent device from sleeping

### Settings Integration
- [ ] Toggle state persists across app restarts
- [ ] Changing prayer calculation method updates notification
- [ ] Changing location updates notification

---

## File Structure

```
app/src/main/java/com/example/quranapp/
├── service/
│   └── PrayerForegroundService.kt          # Main notification service
├── receiver/
│   └── PrayerServiceBootReceiver.kt        # Boot restart receiver
├── presentation/screen/home/
│   ├── PrayerNotificationToggle.kt         # Toggle UI component
│   ├── PrayerSettingsDialog.kt             # Modified (added Notif tab)
│   ├── HomeScreen.kt                       # Modified (pass repository)
│   └── HomeScreenViewModel.kt              # Modified (expose repository)
└── data/repository/
    └── PrayerSettingsRepository.kt         # Modified (added prefs methods)

app/src/main/
└── AndroidManifest.xml                      # Modified (permissions & components)

app/
└── build.gradle.kts                         # Modified (added accompanist-permissions)

gradle/
└── libs.versions.toml                       # Modified (added accompanist-permissions)
```

---

## Dependencies Added

### Gradle Catalog (libs.versions.toml)
```toml
[versions]
accompanistPermissions = "0.36.0"

[libraries]
accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanistPermissions" }
```

### App Build (build.gradle.kts)
```kotlin
implementation(libs.accompanist.permissions)
```

---

## Future Enhancements (Optional)

1. **Customizable Update Intervals**
   - Allow users to choose update frequency (30s, 1min, 5min)

2. **Silent Hours**
   - Allow users to disable notification during certain hours

3. **Rich Notification**
   - Add actions (e.g., "Snooze", "View Prayer Times")
   - Show all prayer times in expanded view

4. **Notification Sound Near Prayer**
   - Optional: Play adhan or reminder sound when prayer time is near

5. **Widget Integration**
   - Sync notification updates with home screen widget

---

## Troubleshooting

### Notification Not Appearing
1. Check notification permission (Settings > Apps > QuranApp > Notifications)
2. Check battery optimization (Settings > Battery > App battery usage)
3. Verify location permission is granted
4. Check if location data is available

### Service Not Restarting After Reboot
1. Verify RECEIVE_BOOT_COMPLETED permission is granted
2. Check if notification was enabled before reboot
3. Check device manufacturer's battery optimization settings
4. Some manufacturers (Xiaomi, Huawei) have aggressive battery savers

### Countdown Not Updating
1. Check if app is in battery optimization whitelist
2. Verify service is running (Settings > Apps > QuranApp > App info)
3. Check if Doze mode is interfering (rare but possible)

---

## Play Store Submission Notes

When submitting to Play Store, include in description:

> **Prayer Countdown Notification**
> Enable an optional persistent notification that shows the time remaining until the next prayer. This feature:
> - Is completely optional and user-controlled
> - Can be enabled/disabled anytime from settings
> - Uses minimal battery (updates once per minute)
> - Provides at-a-glance prayer time awareness
> - Automatically restarts after device reboot (if enabled)

---

## Compliance Statement

This implementation follows all Android and Play Store guidelines:

✅ Foreground service has clear user benefit
✅ Notification is always visible when service runs
✅ User has full control to enable/disable
✅ No hidden background activities
✅ Battery-efficient implementation
✅ Proper permission handling
✅ Transparent behavior

**Last Updated**: December 30, 2025
**Implementation Version**: 1.0

