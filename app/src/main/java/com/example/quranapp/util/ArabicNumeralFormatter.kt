package com.example.quranapp.util

/**
 * Shared utility for converting Western (ASCII) digits to Arabic-Indic numerals.
 *
 * Arabic-Indic digits (٠‑٩) start at Unicode code-point U+0660.
 * The offset from ASCII '0' (U+0030) is constant, so a simple char
 * arithmetic conversion is used instead of a lookup map.
 *
 * This is the single source-of-truth for numeral formatting across
 * the whole application – every screen should call this function
 * rather than maintaining its own private copy.
 */
object ArabicNumeralFormatter {

    private const val ARABIC_ZERO: Char = '٠' // U+0660

    /**
     * Converts an [Int] to a string of Arabic-Indic numerals.
     *
     * ```
     * convertToArabicNumerals(123)  // "١٢٣"
     * convertToArabicNumerals(0)    // "٠"
     * ```
     */
    fun convertToArabicNumerals(number: Int): String {
        val str = number.toString()
        val sb = StringBuilder(str.length)
        for (ch in str) {
            sb.append(if (ch in '0'..'9') (ARABIC_ZERO + (ch - '0')) else ch)
        }
        return sb.toString()
    }
}

