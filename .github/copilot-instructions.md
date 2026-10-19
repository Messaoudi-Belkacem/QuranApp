# Copilot Instructions for QuranApp

## Build, test, and lint

Run all commands from the repository root on Windows:

```powershell
# Build debug app
.\gradlew.bat :app:assembleDebug

# Unit tests (local JVM)
.\gradlew.bat :app:testDebugUnitTest

# Run a single unit test
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.quranapp.ExampleUnitTest.addition_isCorrect"

# Instrumentation tests (requires connected device/emulator)
.\gradlew.bat :app:connectedDebugAndroidTest

# Run a single instrumentation test
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.quranapp.ExampleInstrumentedTest#useAppContext

# Lint
.\gradlew.bat :app:lintDebug
```

## High-level architecture

- **App shell:** `MainActivity` hosts Compose and `RootNavigationGraph`. `MainScreen` creates a second, inner nav graph (`MainNavGraph`) for bottom tabs (`Home`, `Quran`, `Qibla`, `More`), while detail/tool screens stay on the root graph.
- **Data flow for Quran content:** Quran text is loaded from `app/src/main/assets/quran_data.json` via `QuranJsonLoader` (with in-memory caches). `QuranRepositoryImpl` serves Surahs/Ayahs from this loader and stores bookmarks via Room.
- **Persistence split (important):**
  - Room: bookmarks entities/DAO (`bookmarks` table).
  - `SharedPreferences` (`quran_prefs`): first-launch and current location (`current_location`).
  - `SharedPreferences` (`prayer_settings`): prayer calculation method, Asr/high-latitude method, prayer notification toggle.
  - DataStore (`inno_app_preferences`): feature-specific prefs (e.g., Tasbih/Adhkar repositories).
- **Prayer stack integration:** Prayer times are calculated from current location + prayer settings (`AdhanPrayerTimeCalculator` + `PrayerSettingsRepository`) and reused by:
  - `HomeScreenViewModel` (main app UI),
  - `PrayerForegroundService` (persistent notification countdown),
  - `PrayerTimesWidget` (Glance widget).
- **Widget lifecycle:** `PrayerTimesWidgetReceiver` schedules/cancels periodic updates through `WidgetUpdateWorker` (WorkManager unique periodic work every 15 minutes).
- **DI model:** Hilt is used app-wide (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`) with modules in `app/src/main/java/com/example/quranapp/di`.

## Key conventions in this repository

- **Route definition pattern:** Add/update routes in `presentation/navigation/Screen.kt` and wire them in the appropriate graph:
  - tab-level destinations in `MainNavGraph`,
  - app-level/detail/tool destinations in `RootNavigationGraph`.
- **State pattern in ViewModels:** Most screens use `MutableStateFlow` + immutable `UiState` data classes; mutate state with `.copy(...)` and expose read-only `StateFlow`.
- **Localization behavior is runtime-applied:** Language changes go through `LanguageManager`; `MainActivity` applies language in `attachBaseContext` and `onCreate`, and settings-triggered language changes restart the activity to apply.
- **Keep bilingual resource parity:** When adding user-facing strings, update both `res/values/strings.xml` and `res/values-ar/strings.xml`.
- **Location key contract:** The stored location format/key in `quran_prefs` (`current_location`) is a shared contract across repository, home/prayer logic, widget, and foreground service. Do not change key/serialization in only one place.
- **Prayer settings consistency:** Any feature that changes prayer calculation settings should keep widget/notification consumers in sync (they read from `PrayerSettingsRepository` and shared location).
