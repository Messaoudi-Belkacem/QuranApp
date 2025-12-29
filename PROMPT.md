> Implement Islamic prayer time calculations using geographic coordinates (latitude, longitude), date, timezone, and elevation. allow the user to choose from various established calculation methods and juristic options for Asr prayer. or use the location's default method based on country.
>
> The system must support multiple **calculation methods** defined by solar depression angles:
>
> **Methods**
>
> * Muslim World League (MWL): Fajr −18°, Isha −17°
> * Egyptian Authority: Fajr −19.5°, Isha −17.5°
> * Umm al-Qura (Makkah): Fajr −18.5°, Isha = Maghrib + 90 min (120 min in Ramadan)
> * ISNA: Fajr −15°, Isha −15°
> * Karachi: Fajr −18°, Isha −18°
>
> **Asr Juristic Methods**
>
> * Shafi’i/Maliki/Hanbali: shadow factor = 1
> * Hanafi: shadow factor = 2
>
> **High-Latitude Adjustments**
>
> * Angle-based night portion
> * Middle of the night
> * One-seventh of the night
>
> **Calculation Rules**
>
> * Dhuhr = solar noon
> * Sunrise/Sunset when sun altitude = −0.833°
> * Maghrib = sunset
> * Fajr/Isha when sun altitude reaches the method’s angle
>
> Provide clean, reusable functions and ensure correct handling of high latitudes and time zones.

---

## How These Methods Work (Engineering Explanation)

### 1. Inputs (Required)

* Latitude, Longitude
* Date
* Timezone
* Calculation method
* Asr juristic method

---

### 2. Astronomical Core (Shared by All Methods)

All methods rely on computing the **Sun’s position**:

* **Solar Declination (δ)**
  Angle between the Sun and Earth’s equator.

* **Equation of Time (EoT)**
  Corrects clock time to true solar time.

* **Solar Noon (Dhuhr)**

  ```
  Dhuhr = 12:00 + timezone − longitude/15 − EoT
  ```

This is the anchor point for all other prayers.

---

### 3. Sunrise & Sunset

Computed when:

```
Sun altitude = −0.833°
```

(The value accounts for atmospheric refraction and solar radius.)

* **Sunrise** → start of daylight
* **Sunset** → **Maghrib**

---

### 4. Fajr & Isha (Method-Dependent)

Each method defines when twilight begins/ends using a **solar depression angle**:

```
cos(H) = (sin(angle) − sin(lat)·sin(dec)) / (cos(lat)·cos(dec))
```

* **Fajr** → Sun reaches Fajr angle before sunrise
* **Isha** → Sun reaches Isha angle after sunset

Smaller angles = **later Fajr, earlier Isha**.

---

### 5. Asr (Fiqh-Based, Not Astronomical Angle)

Asr is calculated using **shadow length**:

```
tan(altitude) = 1 / (factor + tan(|lat − declination|))
```

* Factor = 1 → Shafi’i, Maliki, Hanbali
* Factor = 2 → Hanafi

This makes Hanafi Asr later.

---

### 6. High-Latitude Handling (Critical)

When Fajr or Isha **never occur** (e.g., near poles):

* **Angle-Based:**

  ```
  NightPortion = angle / 60
  ```
* **Middle of Night:**
  Night ÷ 2
* **One-Seventh:**
  Night ÷ 7

Your app must apply one of these automatically when angles fail.

---

## Minimal Method Set You Should Support (Recommended)

For a production app:

1. **MWL** (global default)
2. **Umm al-Qura** (Saudi users)
3. **ISNA** (North America)
4. **Egyptian** (Africa / Middle East)
5. **Asr: Standard + Hanafi**
6. **High-Latitude: Angle-Based**

This covers **95% of users worldwide**.

---

## Architecture Tip (Clean Design)

```kotlin
data class CalculationMethod(
    val fajrAngle: Double?,
    val ishaAngle: Double?,
    val ishaInterval: Int? // minutes after Maghrib
)
```

Keep astronomy **separate** from fiqh logic.