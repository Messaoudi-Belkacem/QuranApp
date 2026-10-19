package com.example.quranapp.util

/**
 * Utility for sanitizing and normalizing Quranic text for proper OpenType rendering.
 *
 * Solves known font-shaping issues in KFGQPC and other Uthmanic fonts:
 * 1. Fatḥah + Dagger Alif collision: When `\u064E` (Fatḥah) immediately precedes
 *    `\u0670` (Superscript/Dagger Alif), text shapers like HarfBuzz anchor both to the
 *    same base consonant coordinate because OpenType `mkmk` tables lack a rule for `0670` over `064E`.
 *    This causes the Fatḥah glyph to overlap and completely hide the Dagger Alif (e.g. in
 *    "ٱلۡعَٰلَمِينَ" and "ٱلرَّحۡمَٰنِ"). Normalizing `\u064E\u0670` -> `\u0670` allows the
 *    Dagger Alif to attach cleanly via Mark-to-Base.
 * 2. Missing Shaddah on Lafz al-Jalālah (e.g., "لِلهِ" -> "لِلَّهِ").
 */
object QuranTextSanitizer {

    private const val FATHA = "\u064E"
    private const val DAGGER_ALIF = "\u0670"
    private const val FATHA_THEN_DAGGER_ALIF = "$FATHA$DAGGER_ALIF"

    /**
     * Sanitizes Quranic text for clean, collision-free Uthmanic font rendering.
     */
    fun sanitize(text: String): String {
        if (text.isBlank()) return text

        return text
            // Normalize redundant Fatha before Dagger Alif so Dagger Alif renders cleanly
            .replace(FATHA_THEN_DAGGER_ALIF, DAGGER_ALIF)
            // Ensure proper Shaddah on Lafz al-Jalalah if missing
            .replace("لِلهِ", "لِلَّهِ")
            .trim()
    }
}
