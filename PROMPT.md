**Prompt – TasbihScreen (QuranApp)**

Design and implement a **“TasbihScreen”** for an Android app called **QuranApp**, written in **Kotlin**, using **Jetpack Compose** (or modern Android UI toolkit) and strictly following **Material Design 3 (Material You) guidelines**.

The purpose of this screen is to let the user perform **digital tasbih / dhikr** in a focused, beautiful, and intuitive way.

### 1. Overall Concept and Layout

- The **TasbihScreen** should feel:
    - Clean, calm, and spiritual.
    - Minimal but still informative.
    - Easy to use with **one hand** and optimized for **thumb tapping**.
- Visual style:
    - Follow **Material Design 3**:
        - Use Material theme colors (primary, secondary, surface, background, error, etc.).
        - Use Material typography styles (TitleLarge, BodyMedium, LabelSmall, etc.).
        - Use Material components (TopAppBar, FloatingActionButton, Buttons, Slider, Dialog, etc.).
    - Consider a **soft, spiritual color palette** (e.g., shades of green, teal, or blue) but still fully compatible with Material theme (light and dark modes).

### 2. Main Structure

- Use a **Scaffold** layout with:
    - **Top App Bar (Material 3)**:
        - Title: “Tasbih” (or localized equivalent).
        - Optional navigation icon (e.g., back arrow) on the left if this screen is not the root.
        - Optional overflow menu (3 dots) on the right for actions like:
            - Reset counter.
            - Settings.
            - History.
    - **Content area**:
        - Central circular count display and main tap area.
        - Current dhikr/phrase information.
        - Controls to switch tasbih presets and adjust target.
    - **Bottom area**:
        - Could be:
            - A bottom bar with quick actions, or
            - A Floating Action Button for additional features (e.g., voice/vibration toggle), respecting Material 3 specs.

### 3. Core Features & Interactions

1. **Current Count Display**
    - Large, prominent **counter number** in the center.
    - Use **Material typography** (e.g., `displayMedium` or `headlineLarge`) for the number.
    - The count should increment when the user taps a large tappable area (see below).
    - Animate the number slightly on each increment (e.g., scale, fade-in) following Material motion guidelines (comfortable, not distracting).

2. **Large Tappable Area (Main Button)**
    - A large circular button or surface in the center of the screen, following Material design for buttons or `Surface` with elevation.
    - This area is the **primary action**: increment the tasbih counter.
    - Design for **ergonomics**:
        - Place it in the middle/lower middle of the screen so it’s easy to tap with a thumb.
    - Visual feedback:
        - On tap, show Material ripple effect.
        - Optional micro animation (e.g., slight press-in, scale).
    - Accessibility:
        - Ensure sufficient size for touch targets (at least 48x48 dp).

3. **Current Dhikr / Phrase Display**
    - Above or below the main counter:
        - Show the **name of the tasbih/dhikr** (e.g., “Subhanallah”, “Alhamdulillah”, “Allahu Akbar”) and possibly its **translation**.
    - Styling:
        - Phrase name in a clear font (e.g., `titleMedium` or `titleLarge`).
        - Translation or extra info in smaller text (`bodyMedium` or `bodySmall`).
    - If Arabic is used:
        - Ensure correct right‑to‑left (RTL) support and proper font (if available).

4. **Target Count and Progress**
    - Allow the user to set a **target** (e.g., 33, 100, 99, custom).
    - Show **progress** towards that target:
        - For example, a **circular progress indicator** around the main button, or a horizontal progress bar underneath.
    - Display target and progress labels:
        - Example: `27 / 33` next to or under the progress indicator.
    - Behavior:
        - When the user reaches the target, provide clear feedback:
            - Subtle haptic vibration (if enabled).
            - Small success animation or color highlight (following Material motion).
            - Optionally show a small snackbar: “Target completed”.

5. **Preset Tasbih Modes**
    - Allow the user to switch between **presets**:
        - Example presets:
            - “Subhanallah” – target 33
            - “Alhamdulillah” – target 33
            - “Allahu Akbar” – target 34
            - “La ilaha illallah” – target 100
            - “Custom” – user-defined name and target.
    - UI for presets:
        - Could be a **horizontal Chip row** (Material filter/assist chips) below the counter.
        - Or a **dropdown menu** / bottom sheet listing available presets.
    - When switching presets:
        - Ask the user if they want to **reset** the count or keep the current count (optional dialog or snackbar action).
        - Make transition smooth and visually clear.

6. **Reset and Undo**
    - Provide a **Reset** action:
        - Reset button in the top app bar menu or a small button/icon near the counter.
        - Show a **confirmation dialog** before resetting to prevent accidental loss:
            - “Reset counter?” with “Cancel” and “Reset” Material buttons.
    - Provide **Undo Last Tap** (optional but useful):
        - Maybe in an overflow menu or as a small icon next to the counter (e.g., a back arrow icon).
        - Must follow Material button/icon size and touch-target guidelines.

7. **Settings for TasbihScreen**
    - Include a way to open a **Tasbih Settings** panel (could be another screen or a bottom sheet).
    - Settings might include:
        - **Haptic feedback** (on/off) when tapping or completing target.
        - **Sound feedback** (on/off), like a soft click or short tone.
        - **Auto-reset on target completion** (on/off).
        - Default **preset** or **default target**.
    - Use Material components for settings (Switch, Checkbox, Radio Buttons, Sliders) as appropriate.

8. **History / Sessions (Optional Advanced Feature)**
    - Optionally show a **small history summary**:
        - Total counts today.
        - Total counts this week.
    - If implemented:
        - Use a simple card or list at the bottom of the screen.
        - Follow Material card designs with proper elevation, padding, and typography.

### 4. Material Design 3 Compliance (Very Important)

The TasbihScreen must **strictly follow Material Design 3 (Material You)**:

- Use standard Material components:
    - `Scaffold`, `TopAppBar`, `FloatingActionButton`, `Button`, `IconButton`, `Card`, `Surface`, `Text`, `Slider`, `Switch`, `Dialog`, `Snackbar`, `ProgressIndicator`, `Chips`, etc.
- **Theming**:
    - Use the app’s Material theme for colors, typography, shapes, and elevation.
    - Support **light and dark modes** automatically via the theme.
- **Typography**:
    - Use Material 3 text styles (e.g., `headlineLarge`, `titleMedium`, `bodyMedium`, `labelSmall`) instead of custom ad-hoc styling.
- **Shapes & Elevation**:
    - Respect Material shape tokens (rounded corners, etc.).
    - Use elevation for main interactive surfaces like the big tasbih tap button.
- **Motion**:
    - Use smooth, meaningful animations consistent with Material motion guidelines.
    - Avoid excessive or fast animations that distract from dhikr.

### 5. Accessibility & Localization

- Accessibility:
    - Ensure large touch targets (≥ 48dp).
    - High contrast between text and background to respect WCAG contrast recommendations.
    - ContentDescription for icons and important visuals for screen readers.
- Localization:
    - All text should be easy to localize (e.g., using string resources, not hardcoded).
    - Support **RTL layouts** correctly for Arabic or other RTL languages.
    - Dhikr phrases may be in Arabic with translations in the user’s language.

### 6. State Management & Behavior

- The TasbihScreen should maintain:
    - Current count.
    - Selected preset.
    - Target count.
    - Settings (haptic, sound, auto-reset, etc.).
- The screen should **preserve state** on configuration changes (e.g., orientation change, dark/light toggle).
- If the user leaves the screen and comes back, the **current count and selection must persist** (e.g., through ViewModel or equivalent state holder).

### 7. Edge Cases & Details

- Handle when the current count exceeds the target:
    - Either allow exceeding and only highlight when reaching the target for the first time.
    - Or auto-reset/loop based on user settings.
- If there is **no target** (target = 0 or disabled):
    - Hide or dim the progress indicator and just show the main counter.
- Errors:
    - If something fails (e.g., loading presets), show a Material **Snackbar** with a short, helpful message.

---

**Deliverable**

Produce the complete UI design and implementation for this **TasbihScreen** in Kotlin, using Jetpack Compose and Material Design 3. The code should:

- Follow Kotlin and Compose best practices.
- Use proper Material components and theming.
- Implement all the features and behaviors described above.
- Be ready to integrate into the existing QuranApp project as `TasbihScreen`.