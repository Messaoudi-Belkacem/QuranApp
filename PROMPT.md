## Improved prompt (clean and precise)

> Create a **MoreScreen** that serves as a secondary navigation hub.
> The screen should display a structured list of options including **Adhkar**, **Tasbih**, **Settings**, **Help**, and **About**.
> Each item must be tappable and navigate to its corresponding screen using the app’s navigation system.

---

## Enhanced version (better UX thinking)

> Design and implement a **MoreScreen** as a secondary navigation area for non-core features.
> The screen should present a vertically scrollable list grouped by purpose:
>
> **Worship Tools**
>
> * Adhkar
> * Tasbih
>
> **App & Support**
>
> * Settings
> * Help
> * About
>
> Each list item should include an icon, a title, and optional descriptive text, and should navigate to its respective screen when selected.

---

## Bright ideas to elevate the More screen

### 1. Logical grouping (very important)

Grouping improves clarity and reduces cognitive load:

* **Ibadah**: Adhkar, Tasbih
* **Application**: Settings
* **Support & Info**: Help, About

This mirrors how users *mentally classify* features.

---

### 2. Add subtle contextual hints

Small subtitles increase discoverability:

* **Adhkar** – Morning & evening remembrances
* **Tasbih** – Digital counter for dhikr
* **Settings** – Language, theme, notifications
* **Help** – FAQs and guidance
* **About** – App mission and version

---

### 3. Respect Islamic tone

* Use calm wording
* Avoid clutter
* Prefer simple icons (tasbih beads, book, info circle)
* Neutral colors consistent with your app’s theme

---

### 4. Optional but valuable additions

If relevant to your app:

* **Share the app**
* **Feedback**
* **Privacy Policy**
* **Version number** at the bottom (small text)

---

### 5. UX best practice

* This screen **must not feel as important as Home**
* No FAB
* No heavy animations
* Simple `LazyColumn` with clear separators

---

## One-line design rule

**MoreScreen = utility, clarity, and calm.**

Remember to read how the apps navigation is structured to ensure seamless integration.