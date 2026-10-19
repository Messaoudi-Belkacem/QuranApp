package com.example.quranapp.presentation.screen.page

import kotlin.math.sqrt

/**
 * Result metrics computed by [PageTypographyEngine] for a single Mushaf page.
 */
data class PageTypographyMetrics(
    val fontSizeSp: Float,
    val lineHeightSp: Float,
    val headerSpacingDp: Float,
    val contentPaddingDp: Float,
    val estimatedLines: Float = 15f,
    val targetLines: Int = kotlin.math.round(estimatedLines).toInt().coerceAtLeast(1)
)

/**
 * Intelligent typography sizing and line-fitting engine for Quran pages.
 *
 * Uses aspect-ratio adaptive area-fill optimization to maximize readability (large font size)
 * and perfectly fill the available screen height and width without leaving dead blank space
 * or clipping lines at the bottom.
 */
object PageTypographyEngine {

    const val MIN_FONT_SIZE_SP = 14.0f
    const val MAX_FONT_SIZE_SP = 24.0f
    const val MIN_LINE_HEIGHT_SP = 24.0f
    const val MAX_LINE_HEIGHT_SP = 50.0f

    // Average character advance in Uthmanic Hafs font (~0.248 em per char)
    private const val AVG_CHAR_ADVANCE_EM = 0.248f

    // Standard aesthetic line-height to font-size ratio for classic Quranic script
    private const val TARGET_LINE_HEIGHT_RATIO = 1.90f

    // Component vertical footprints
    private const val COMPACT_SURAH_HEADER_HEIGHT_DP = 34f
    private const val MULTI_SURAH_HEADER_HEIGHT_DP = 26f
    private const val COMPACT_BISMILLAH_HEIGHT_DP = 26f
    private const val MULTI_BISMILLAH_HEIGHT_DP = 20f
    private const val PAGE_FOOTER_HEIGHT_DP = 26f
    private const val TOP_INFO_HEIGHT_DP = 18f

    fun calculateTypography(
        availableHeightDp: Float,
        availableWidthDp: Float,
        hasSurahHeader: Boolean = false,
        hasBismillah: Boolean = false,
        ayahCount: Int = 0,
        totalCharacterCount: Int = 0,
        headerCount: Int = if (hasSurahHeader) 1 else 0,
        bismillahCount: Int = if (hasBismillah) 1 else 0,
        mushafLines: Int? = null
    ): PageTypographyMetrics {
        val effectiveHeaderCount = if (headerCount > 0) headerCount else if (hasSurahHeader) 1 else 0
        val effectiveBismillahCount = if (bismillahCount > 0) bismillahCount else if (hasBismillah) 1 else 0

        val headerHeight = effectiveHeaderCount * (if (effectiveHeaderCount > 1) MULTI_SURAH_HEADER_HEIGHT_DP else COMPACT_SURAH_HEADER_HEIGHT_DP)
        val bismillahHeight = effectiveBismillahCount * (if (effectiveBismillahCount > 1) MULTI_BISMILLAH_HEIGHT_DP else COMPACT_BISMILLAH_HEIGHT_DP)
        val footerHeight = PAGE_FOOTER_HEIGHT_DP
        val topInfoHeight = TOP_INFO_HEIGHT_DP

        val compactPadding = if (availableHeightDp < 600f || effectiveHeaderCount > 1) 6f else 8f
        val compactSpacing = (effectiveHeaderCount + effectiveBismillahCount) * 2f

        val totalNonTextHeight = headerHeight + bismillahHeight + footerHeight + topInfoHeight + (compactPadding * 2) + compactSpacing
        val availableTextHeight = (availableHeightDp - totalNonTextHeight).coerceAtLeast(100f)

        // Usable horizontal width for Quranic text
        val usableWidth = (availableWidthDp - (compactPadding * 2)).coerceAtLeast(100f)

        val effectiveCharCount = if (totalCharacterCount > 0) totalCharacterCount else 1150

        // Quadratic Area-Fill Formula:
        // Text height H = lines * lineH = (chars * AVG_EM * F / W) * (ratio * F)
        // Therefore F = sqrt( H * W / (chars * AVG_EM * ratio) )
        val idealFontSize = sqrt(
            (availableTextHeight * usableWidth) / (effectiveCharCount * AVG_CHAR_ADVANCE_EM * TARGET_LINE_HEIGHT_RATIO)
        )

        // Clamp font size to large readable bounds
        val clampedFontSize = idealFontSize.coerceIn(MIN_FONT_SIZE_SP, MAX_FONT_SIZE_SP)

        // Calculate expected number of lines with this font size
        val estimatedLines = ((effectiveCharCount * AVG_CHAR_ADVANCE_EM * clampedFontSize) / usableWidth).coerceAtLeast(1f)

        // Calculate line height that distributes lines to fill the available height perfectly
        val idealLineHeight = availableTextHeight / estimatedLines
        val clampedLineHeight = idealLineHeight.coerceIn(
            clampedFontSize * 1.55f,
            clampedFontSize * 2.25f
        ).coerceIn(MIN_LINE_HEIGHT_SP, MAX_LINE_HEIGHT_SP)

        return PageTypographyMetrics(
            fontSizeSp = clampedFontSize,
            lineHeightSp = clampedLineHeight,
            headerSpacingDp = 2f,
            contentPaddingDp = compactPadding,
            estimatedLines = estimatedLines
        )
    }
}
