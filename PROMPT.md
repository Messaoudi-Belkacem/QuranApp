Below is a **short, practical step-by-step** process tailored for **Jetpack Compose**.

### Step 1: Create string resources (English)

Create or open:

```
res/values/strings.xml
```

Move every hardcoded text into it:

```xml
<string name="login">Login</string>
<string name="welcome">Welcome</string>
```

### Step 2: Replace hardcoded text in Compose

Before:

```kotlin
Text("Welcome")
```

After:

```kotlin
Text(stringResource(R.string.welcome))
```

Repeat this for **all Text, Button, Snackbar, Dialog, etc.**

### Step 3: Create Arabic strings file

Create:

```
res/values-ar/strings.xml
```

Add Arabic translations:

```xml
<string name="login">تسجيل الدخول</string>
<string name="welcome">مرحباً</string>
```

### Step 4: Enable RTL support

In `AndroidManifest.xml`:

```xml
android:supportsRtl="true"
```

Use `start` / `end` padding and alignment (not `left` / `right`).

### Step 5: Test

* Change device language to Arabic → Arabic loads automatically
* Change back to English → English loads automatically

### Step 6 (Optional): In-app language switch

If needed later, apply locale using `AppCompatDelegate.setApplicationLocales()`.

### Final Rule (Important)

**Never hardcode text in Compose again.**
Always use `stringResource()`.