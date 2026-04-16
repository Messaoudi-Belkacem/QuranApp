# 📖 Quran App Design System

## 1. Overview

This design system defines the visual language, UI components, and interaction principles for a modern Quran application. The goal is to create a **calm, readable, and spiritually respectful** experience while maintaining modern UX standards.

---

## 2. Design Principles

* **Clarity First** → Prioritize readability of Arabic text
* **Spiritual Minimalism** → Avoid visual noise
* **Consistency** → Unified components and spacing
* **Accessibility** → Support all users (font scaling, contrast, RTL)
* **Performance-Oriented** → Smooth scrolling and fast rendering

---

## 3. Color System

### Primary Colors

* **Deep Green** `#0F3D2E` → Spiritual anchor, main branding
* **Emerald** `#1F7A63` → Interactive elements
* **Gold Accent** `#C9A646` → Highlights (Ayah numbers, important UI)

### Neutral Colors

* **Background Light** `#F8F9F6`
* **Background Dark** `#121212`
* **Surface** `#FFFFFF` / `#1E1E1E`
* **Divider** `#E0E0E0`

### Semantic Colors

* Success: `#4CAF50`
* Warning: `#FF9800`
* Error: `#F44336`

---

## 4. Typography

### Arabic Font (Primary)

* **Amiri / Uthmanic Script / Scheherazade**
* Use for Quran text only

### UI Font (Secondary)

* **Inter / Roboto**
* Used for UI, translations, metadata

### Type Scale

* Quran Text: `24–32px`
* Translation: `14–18px`
* Titles: `18–24px`
* Caption: `12–14px`

### Rules

* Maintain **high line height (1.8–2.2)** for Arabic readability
* Support **dynamic font scaling**

---

## 5. Layout & Spacing

### Grid System

* 8pt spacing system

### Spacing Scale

* XS: 4px
* SM: 8px
* MD: 16px
* LG: 24px
* XL: 32px

### Layout Guidelines

* Generous padding for reading comfort
* Avoid cramped layouts
* Center focus on Quran content

---

## 6. Core Components

### 6.1 Ayah Card

* Arabic text (primary focus)
* Translation below
* Ayah number badge (gold accent)
* Actions:

    * Bookmark
    * Share
    * Audio play

---

### 6.2 Audio Player

* Play / Pause
* Progress bar
* Reciter selection
* Playback speed

---

### 6.3 Navigation Bar

* Home
* Surahs
* Search
* Bookmarks
* Settings

---

### 6.4 Surah List Item

* Surah name (Arabic + English)
* Number
* Revelation type (Meccan / Medinan)

---

### 6.5 Search

* Instant search
* Highlight matching words
* Support Arabic + translation

---

## 7. Interaction Design

### Gestures

* Swipe → next/previous Ayah or page
* Long press → Ayah actions
* Tap → show/hide translation

### Animations

* Subtle and smooth (200–300ms)
* Avoid distracting transitions

---

## 8. Dark Mode

* Use **true dark background (`#121212`)**
* Reduce brightness of white text
* Maintain contrast for readability
* Gold accents slightly muted

---

## 9. Accessibility

* RTL support (mandatory)
* Adjustable font size
* High contrast mode
* Screen reader support
* Audio alternatives for all content

---

## 10. Icons & Visual Style

* Use **simple outline icons**
* Avoid overly decorative elements
* Islamic patterns only as subtle backgrounds
* No excessive ornamentation

---

## 11. Performance Guidelines

* Lazy load Surahs
* Cache audio files
* Optimize font rendering
* Avoid heavy animations in reading view

---

## 12. Future Enhancements

* Tafsir integration
* Word-by-word breakdown
* AI-powered search
* Personalized reading goals

---

## 13. Summary

This design system ensures the Quran app is:

* **Readable**
* **Respectful**
* **Modern**
* **Accessible**

The Quran content must always remain the **central focus**, with UI serving as a quiet, supportive layer.
