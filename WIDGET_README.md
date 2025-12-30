# Prayer Times Widget

## Overview
A beautiful Android home screen widget that displays:
- **Time remaining** until the next prayer
- **Time passed** since the last prayer
- **Prayer times** with accurate calculations
- **Auto-updates** every 15 minutes

## Features

### Visual Design
- 🎨 **Beautiful gradient background** (blue ocean gradient)
- 🕌 **Mosque icon** header
- ⏰ **Large, readable time displays**
- 🔄 **Real-time countdown** to next prayer
- 📊 **Time elapsed** since last prayer
- 🌙 **Dark theme optimized** for battery and visibility

### Functionality
- ✅ Uses **Adhan library** for accurate prayer times
- ✅ Respects user's **calculation method** settings
- ✅ **Automatic updates** every 15 minutes via WorkManager
- ✅ **Tap to open app** functionality
- ✅ Shows **last update time**
- ✅ Handles **no location** gracefully

## How to Add Widget

1. **Long press** on your home screen
2. Tap **Widgets**
3. Find **Muwahid - Prayer Times**
4. **Drag and drop** to your home screen
5. Widget will automatically update with your prayer times

## Widget Display

```
┌─────────────────────────────────┐
│ 🕌 Prayer Times    Updated: 12:00│
├─────────────────────────────────┤
│ NEXT PRAYER                      │
│ ⏰  Dhuhr              in         │
│     12:30 PM         2h 15m      │
├─────────────────────────────────┤
│ LAST PRAYER                      │
│ 🔄  Fajr                   ago   │
│                         4h 30m   │
└─────────────────────────────────┘
```

## Technical Details

### Files Created

**Widget Provider:**
- `PrayerTimesWidgetProvider.kt` - Main widget logic
- `WidgetUpdateWorker.kt` - Periodic update worker

**Layouts:**
- `prayer_times_widget.xml` - Widget layout
- `widget_background.xml` - Gradient background
- `prayer_times_widget_info.xml` - Widget configuration

**Icons:**
- `ic_mosque.xml` - Mosque/minaret icon
- `ic_prayer_time.xml` - Clock icon
- `ic_history.xml` - History/past icon

### Update Frequency

- **System updates**: Every 30 minutes (Android AppWidget default)
- **WorkManager updates**: Every 15 minutes (for more frequent updates)
- **Manual updates**: When app refreshes prayer times
- **On settings change**: Immediate update

### Size
- **Minimum**: 250dp x 200dp
- **Resizable**: Yes (horizontal and vertical)
- **Recommended**: 2x2 or 2x3 grid cells

## Customization

The widget automatically adapts to:
- User's selected **calculation method** (MWL, Egyptian, Makkah, etc.)
- User's **Asr method** (Shafi'i or Hanafi)
- User's **high latitude rule**
- Current **device location**

## Data Privacy

- Widget uses **local calculations** only
- No external API calls for prayer times
- Uses device location from app settings
- All data stored locally

## Troubleshooting

**Widget shows "No Location":**
- Open the app and grant location permission
- Ensure GPS is enabled
- App will calculate and store location
- Widget will update automatically

**Times not updating:**
- Check that battery optimization is disabled for the app
- Ensure the app has been opened at least once
- Widget updates every 15 minutes automatically

**Widget not appearing:**
- Ensure you're running Android 8.0 or higher
- Try restarting your device
- Check app permissions

## Battery Impact

- **Minimal impact**: Updates only every 15 minutes
- **Smart updates**: Uses WorkManager constraints
- **No GPS polling**: Uses cached location from app
- **Optimized**: Calculations done locally, no network needed

## Design Philosophy

The widget follows Material Design 3 principles:
- **Clear hierarchy**: Next prayer is prominent
- **Visual feedback**: Color-coded (green for next, gray for past)
- **Readable**: Large fonts with good contrast
- **Beautiful**: Gradient background with subtle borders
- **Informative**: Shows all necessary information at a glance

## Future Enhancements

Potential future features:
- Multiple widget sizes (1x1, 4x2, etc.)
- Theme options (light/dark/custom)
- Configurable update frequency
- Next prayer notification integration
- Multiple location support

