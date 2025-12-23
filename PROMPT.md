# Navigation Fix: Surah Reading Screen

## Problem
When tapping a Surah in the Quran screen (accessed from bottom navigation), the app crashed with:
```
java.lang.IllegalArgumentException: Navigation destination that matches route 
surah_reading_screen/1 cannot be found in the navigation graph
```

## Root Cause
The app has a nested navigation structure:
- **RootNavigationGraph**: Contains top-level routes including `SurahReadingRoute` 
- **MainNavGraph**: A nested NavHost for bottom navigation tabs (Home, Quran, Qibla, Tasbih)

The issue occurred because `MainNavGraph` was trying to navigate directly to `SurahReadingRoute` using its own local `navController`. However, `SurahReadingRoute` is only defined in the parent `RootNavigationGraph`, not in the nested `MainNavGraph`. This caused the navigation system to throw an exception because the destination couldn't be found in the local navigation graph.

## Solution
The fix involves properly routing navigation calls through the correct navigation controller hierarchy:

### Changes Made

1. **MainNavGraph.kt**
   - Added `onNavigateToSurahReading: (Int) -> Unit` callback parameter
   - Changed `QuranScreen`'s `onSurahClick` to call this callback with the surah ID
   - Removed direct navigation call to `SurahReadingRoute` (which doesn't exist in this NavHost)

2. **MainScreen.kt**
   - Added `rootNavController: NavHostController` parameter
   - Passed `onNavigateToSurahReading` callback to `MainNavGraph` that uses the root controller
   - Now properly navigates using: `rootNavController.navigate(Screen.SurahReadingRoute.createRoute(surahId))`

3. **RootNavigationGraph.kt**
   - Updated `MainScreen` composable call to pass `rootNavController = navHostController`
   - This gives MainScreen access to the navigation controller that has `SurahReadingRoute` defined

### Why This Works
- Navigation now flows from inner nested NavHost up to the parent/root NavHost
- The root controller (which defines `SurahReadingRoute`) handles the actual navigation
- Follows proper nested navigation patterns in Jetpack Compose Navigation

## Testing
1. Launch the app
2. Tap the "Quran" tab in bottom navigation
3. Tap any Surah item in the list
4. **Expected**: App navigates to SurahReadingScreen showing the selected surah
5. **Expected**: No crash occurs
6. Press back button to return to Quran list

## Status
✅ **FIXED** - Navigation between nested NavHost and parent routes now works correctly

