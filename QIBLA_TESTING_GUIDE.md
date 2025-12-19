# Qibla Compass - Testing & Usage Guide

## 🧪 How to Test

### Prerequisites
- **Physical Android device** (emulator won't have compass sensors)
- Android 8.0+ (API 26+)
- GPS/Location services enabled
- Compass sensor available (most modern devices have this)

### Step-by-Step Testing

#### 1. **First Launch - Permission Handling**
```
Expected Behavior:
1. App launches
2. If location permission not granted → Shows permission request dialog
3. User grants permission → App proceeds to main screen
4. User denies permission → Shows permission screen with retry option
```

#### 2. **Navigate to Qibla Screen**
```
Expected Behavior:
1. Tap on "Qibla" tab in bottom navigation
2. Screen shows loading indicator with "Getting your location..."
3. GPS fetches location (may take 5-10 seconds first time)
4. Compass activates and displays
```

#### 3. **Using the Compass**
```
Expected Behavior:
1. Compass background shows N, E, S, W directions
2. Qibla arrow points toward Kaaba
3. As you rotate device, compass rotates smoothly
4. Arrow always points toward Kaaba regardless of device orientation
5. When aligned (±5°), arrow pulses and shows "✓ Aligned with Qibla"
```

#### 4. **Information Display**
```
Check that UI shows:
- "Direction to Kaaba" header
- Bearing in degrees (e.g., "245.3° from North")
- Distance to Kaaba in km (e.g., "Distance: 8,450 km")
- Your GPS coordinates at bottom
- "Lat: XX.XXXX, Lon: YY.YYYY"
```

#### 5. **Refresh Location**
```
Test:
1. Tap "Refresh Location" button
2. Should show loading briefly
3. Updates location and recalculates bearing
4. Useful if user moves to different location
```

## 🔍 Test Cases

### Test Case 1: Permission Already Granted
```
Setup: Grant location permission before launching app
Steps: Launch app → Navigate to Qibla screen
Expected: No permission dialog, directly shows compass
```

### Test Case 2: Permission Denied
```
Setup: Launch fresh install
Steps: Launch app → Deny permission
Expected: Shows permission required screen with explanation
Action: Tap "Grant Permission" → Shows system dialog again
```

### Test Case 3: GPS Disabled
```
Setup: Turn off device GPS
Steps: Launch app → Navigate to Qibla screen
Expected: Shows error "Unable to get location. Please check permissions and GPS."
Action: Tap "Retry" → Re-checks GPS status
```

### Test Case 4: No Compass Sensor
```
Setup: Test on device without compass (rare)
Steps: Navigate to Qibla screen
Expected: Shows "Compass Not Available" message
```

### Test Case 5: Indoor/Poor GPS Signal
```
Setup: Go inside building, poor GPS signal
Steps: Try to load Qibla
Expected: May take longer to load, or show cached location
Action: Can try "Refresh Location" button
```

### Test Case 6: Rotation Smoothness
```
Test: Rotate device 360° slowly
Expected: Compass rotates smoothly without jitter
Note: Arrow should maintain direction to Kaaba
```

### Test Case 7: Alignment Detection
```
Test: Rotate device until arrow points up
Expected: When within ±5° of Qibla:
- Arrow pulses (scale animation)
- Green badge appears: "✓ Aligned with Qibla"
- Center circle highlights in green
```

### Test Case 8: Different Locations
```
Test: Try from different global locations
Examples:
- New York → Should point Northeast (~53°)
- London → Should point Southeast (~118°)
- Tokyo → Should point West-Northwest (~292°)
- Sydney → Should point Northwest (~310°)
- Mecca → Should show ~0 km distance
```

### Test Case 9: Calibration Warning
```
Setup: Some devices need compass calibration
Expected: If accuracy low, shows warning:
"⚠ Calibrate your compass for better accuracy"
Action: Wave device in figure-8 pattern to calibrate
```

## 📊 Validation Checks

### Accuracy Validation
```kotlin
// From different cities, expected bearings to Kaaba:
New York (40.7128, -74.0060) → ~53° (Northeast)
London (51.5074, -0.1278) → ~118° (Southeast)
Jakarta (-6.2088, 106.8456) → ~294° (Northwest)
Sydney (-33.8688, 151.2093) → ~310° (Northwest)
```

### Distance Validation
```kotlin
// Expected distances from major cities:
New York → ~10,250 km
London → ~4,650 km
Jakarta → ~6,800 km
Sydney → ~11,750 km
```

## 🐛 Debugging

### Common Issues

#### Issue: Arrow not rotating
```
Cause: Compass sensor not working
Fix: 
- Check device has compass sensor
- Restart app
- Check sensor in device settings
```

#### Issue: Location not loading
```
Cause: GPS disabled or no permission
Fix:
- Enable GPS in device settings
- Grant location permission
- Go outdoors for better GPS signal
- Tap "Refresh Location"
```

#### Issue: Arrow points wrong direction
```
Cause: Compass needs calibration
Fix:
- Move device in figure-8 pattern
- Move away from magnetic interference
- Check magnetic declination is accounted for
```

#### Issue: Jittery rotation
```
Cause: Sensor interference or low accuracy
Fix:
- Move away from metal objects/magnets
- Calibrate compass
- Ensure device flat and stable
```

## 📱 Device Requirements

### Minimum Requirements
- Android 8.0 (API 26) or higher
- GPS/Location hardware
- Rotation Vector sensor OR (Accelerometer + Magnetometer)
- Location permission granted

### Recommended
- Android 10+ for best sensor accuracy
- Outdoor use for best GPS accuracy
- Compass calibration performed
- No magnetic interference nearby

## 🎯 Expected User Experience

### Normal Flow
```
1. User opens Qibla tab
2. Loading indicator (2-5 seconds)
3. Compass appears with location info
4. User rotates device to find Qibla
5. Arrow smoothly follows Kaaba direction
6. Alignment indicator when pointed correctly
7. User prays in indicated direction
```

### Error Flow
```
1. User opens Qibla tab
2. Error appears (permission/GPS/sensor)
3. Clear error message shown
4. Action button provided (Retry/Grant Permission)
5. User can resolve issue and retry
```

## ✅ Success Criteria

The implementation is successful if:

- ✓ Compass loads within 10 seconds (good GPS signal)
- ✓ Arrow rotates smoothly (no jitter)
- ✓ Direction is accurate (±5° is acceptable)
- ✓ Alignment detection works (pulsing animation)
- ✓ All error states handled gracefully
- ✓ UI is beautiful and intuitive
- ✓ Permission flow works correctly
- ✓ Location updates work on refresh
- ✓ Works in both light and dark mode
- ✓ No crashes or performance issues

## 🌐 Real-World Testing Locations

### Test from Various Locations
```
North America: Should point East/Northeast
South America: Should point East/Northeast  
Europe: Should point East/Southeast
Africa (West): Should point East/Northeast
Africa (East): Should point North/Northeast
Asia (East): Should point West/Northwest
Asia (South): Should point Northwest
Oceania: Should point Northwest
```

### In Mecca
```
Distance should be < 50 km
Any direction acceptable (you're already there!)
```

## 📝 Notes

- First GPS fix may take 10-30 seconds outdoors
- Indoor GPS may be inaccurate or unavailable
- Compass accuracy depends on device sensor quality
- Magnetic interference (metal, electronics) affects accuracy
- Regular calibration improves compass accuracy
- The bearing calculation uses true north (not magnetic north)

## 🎉 What You Should See

When everything works perfectly:

1. 🧭 A beautiful, rotating compass with cardinal directions
2. ⬆️ A smooth, animated arrow pointing toward Kaaba
3. 📍 Your current location coordinates
4. 📏 Distance to Kaaba in kilometers
5. 🎯 Bearing in degrees from north
6. ✨ Pulsing animation when aligned
7. ✅ Green confirmation when pointing correctly
8. 🔄 Working refresh button for location updates

The Qibla compass should feel **smooth**, **responsive**, and **accurate**!

