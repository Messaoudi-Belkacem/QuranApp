## 1. High-level architecture

**Goal:** Show an arrow/compass that always points toward the Kaaba based on:

* User location (GPS)
* Device orientation (sensor)

**Main components**

1. Location provider → latitude & longitude
2. Qibla bearing calculation
3. Orientation sensor (compass/rotation vector)
4. Jetpack Compose UI that rotates an arrow

---

## 2. Get user location

Use **FusedLocationProviderClient**.

**Permissions**

* `ACCESS_FINE_LOCATION`

**Approach**

* Request permission
* Get last known location
* Observe location as `State`

You only need latitude & longitude once unless you want live updates.

---

## 3. Calculate Qibla direction (bearing)

Kaaba coordinates:

```text
Latitude  = 21.4225
Longitude = 39.8262
```

**Formula (bearing in degrees):**

```kotlin
fun calculateQiblaBearing(
    userLat: Double,
    userLon: Double
): Double {
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLon = Math.toRadians(39.8262)

    val lat = Math.toRadians(userLat)
    val lon = Math.toRadians(userLon)

    val dLon = kaabaLon - lon

    val y = Math.sin(dLon)
    val x = Math.cos(lat) * Math.tan(kaabaLat) -
            Math.sin(lat) * Math.cos(dLon)

    return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360
}
```

This gives **Qibla direction relative to true north**.

---

## 4. Get device orientation (compass)

### Recommended sensor

* `TYPE_ROTATION_VECTOR` (more stable than accelerometer + magnetometer)

**Approach**

* Register sensor listener
* Convert rotation matrix → azimuth (0–360°)
* Expose as `State<Float>`

Azimuth = direction device is facing (north reference).

---

## 5. Combine orientation + Qibla bearing

```kotlin
val rotationAngle = qiblaBearing - deviceAzimuth
```

This is the **angle your arrow should rotate**.

---

## 6. Jetpack Compose UI

### UI structure

* Compass background (image or Canvas)
* Arrow pointing upward (north)
* Rotate arrow using `Modifier.rotate()`

```kotlin
@Composable
fun QiblaScreen(
    qiblaBearing: Float,
    deviceAzimuth: Float
) {
    val rotation = qiblaBearing - deviceAzimuth

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.compass_bg),
            contentDescription = null,
            modifier = Modifier.size(300.dp)
        )

        Image(
            painter = painterResource(R.drawable.arrow),
            contentDescription = "Qibla Direction",
            modifier = Modifier
                .size(200.dp)
                .rotate(rotation)
        )
    }
}
```

---

## 7. State management (recommended)

* `ViewModel`

    * Holds:

        * `locationState`
        * `qiblaBearing`
        * `azimuth`
* Sensors update azimuth
* Location updates Qibla bearing
* Compose reacts automatically

---

## 8. Accuracy considerations (important)

* Ask user to **calibrate compass**
* Use **magnetic declination** if you want higher accuracy
* Avoid indoor metal interference
* Smooth rotation using `animateFloatAsState()`

```kotlin
val smoothRotation by animateFloatAsState(rotation)
```

---

## 9. Summary flow

```text
Location → Calculate Qibla bearing
Sensor   → Get device azimuth
UI       → Rotate arrow = bearing - azimuth
```

---

## 10. Optional enhancements

* Map preview with Kaaba marker
* Vibration when aligned
* Degree indicator (e.g. “245°”)
* Manual location selection