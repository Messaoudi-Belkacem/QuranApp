package com.example.quranapp.presentation.screen.page

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PageReadingModernizationTest {

    // =========================================================================
    // 1. Page Navigation Policy Tests
    // =========================================================================

    @Test
    fun testPageNavigationPolicy_boundsAndConversions() {
        assertEquals("Min page must be 1", 1, PageNavigationPolicy.MIN_PAGE)
        assertEquals("Max page must be 604", 604, PageNavigationPolicy.MAX_PAGE)

        // Index ⇄ Page Number conversion
        assertEquals(1, PageNavigationPolicy.pageIndexToPageNumber(0))
        assertEquals(6, PageNavigationPolicy.pageIndexToPageNumber(5))
        assertEquals(604, PageNavigationPolicy.pageIndexToPageNumber(603))

        assertEquals(0, PageNavigationPolicy.pageNumberToPageIndex(1))
        assertEquals(5, PageNavigationPolicy.pageNumberToPageIndex(6))
        assertEquals(603, PageNavigationPolicy.pageNumberToPageIndex(604))

        // Clamping
        assertEquals(1, PageNavigationPolicy.clampPage(0))
        assertEquals(1, PageNavigationPolicy.clampPage(-5))
        assertEquals(604, PageNavigationPolicy.clampPage(605))
        assertEquals(604, PageNavigationPolicy.clampPage(1000))
        assertEquals(42, PageNavigationPolicy.clampPage(42))

        // Navigation availability
        assertFalse("Page 1 has no previous page", PageNavigationPolicy.hasPrevious(1))
        assertTrue("Page 1 has next page", PageNavigationPolicy.hasNext(1))
        assertTrue("Page 6 has previous page", PageNavigationPolicy.hasPrevious(6))
        assertTrue("Page 6 has next page", PageNavigationPolicy.hasNext(6))
        assertTrue("Page 604 has previous page", PageNavigationPolicy.hasPrevious(604))
        assertFalse("Page 604 has no next page", PageNavigationPolicy.hasNext(604))

        // Next and Previous calculations
        assertEquals(5, PageNavigationPolicy.getPreviousPage(6))
        assertEquals(7, PageNavigationPolicy.getNextPage(6))
        assertEquals(1, PageNavigationPolicy.getPreviousPage(1))
        assertEquals(604, PageNavigationPolicy.getNextPage(604))
    }

    // =========================================================================
    // 2. Page Typography Engine Tests
    // =========================================================================

    @Test
    fun testPageTypographyEngine_standardPageFitsWithinHeight() {
        // Page 6: Standard 15-line page with ~6 ayahs and ~550 characters, no surah header
        val availableHeightDp = 700f
        val availableWidthDp = 380f

        val metrics = PageTypographyEngine.calculateTypography(
            availableHeightDp = availableHeightDp,
            availableWidthDp = availableWidthDp,
            hasSurahHeader = false,
            hasBismillah = false,
            ayahCount = 6,
            totalCharacterCount = 550
        )

        // Bounds checks
        assertTrue("Font size should be readable (>= 12sp)", metrics.fontSizeSp >= 12f)
        assertTrue("Line height should be between 22 and 50 sp", metrics.lineHeightSp in 22f..50f)
        assertTrue("Line height should be greater than font size", metrics.lineHeightSp > metrics.fontSizeSp)

        // Height budget check: estimated lines * lineHeight + padding must be <= available height
        val estimatedTotalHeight = metrics.estimatedLines * metrics.lineHeightSp + (metrics.contentPaddingDp * 2) + 26f + 18f
        assertTrue(
            "Estimated total height ($estimatedTotalHeight dp) must fit within available height ($availableHeightDp dp)",
            estimatedTotalHeight <= availableHeightDp + 1f
        )
    }

    @Test
    fun testPageTypographyEngine_pageWithHeaderAndBismillahScalesDownToFit() {
        // Page 2: Surah Al-Baqarah start (Header + Bismillah + Ayahs 1-5, ~350 chars)
        val availableHeightDp = 700f
        val availableWidthDp = 380f

        val headerMetrics = PageTypographyEngine.calculateTypography(
            availableHeightDp = availableHeightDp,
            availableWidthDp = availableWidthDp,
            hasSurahHeader = true,
            hasBismillah = true,
            ayahCount = 5,
            totalCharacterCount = 350,
            mushafLines = 6
        )

        // When header & bismillah are present, spacing/padding should be compact
        assertTrue("Header spacing should be compact (<= 12dp)", headerMetrics.headerSpacingDp <= 12f)
        assertTrue("Content padding should be compact (<= 16dp)", headerMetrics.contentPaddingDp <= 16f)

        // Total vertical footprint must safely fit
        val headerFootprint = 34f + 26f + (headerMetrics.headerSpacingDp * 2) + 26f + 18f
        val textFootprint = headerMetrics.estimatedLines * headerMetrics.lineHeightSp
        val totalFootprint = headerFootprint + textFootprint + (headerMetrics.contentPaddingDp * 2)

        assertTrue(
            "Page with header and bismillah ($totalFootprint dp) must fit in available height ($availableHeightDp dp)",
            totalFootprint <= availableHeightDp + 1f
        )
    }

    @Test
    fun testPageTypographyEngine_shortPageClampsToAestheticMax() {
        // Page 1: Surah Al-Fatihah (7 short ayahs, ~299 chars)
        val availableHeightDp = 800f
        val availableWidthDp = 380f

        val metrics = PageTypographyEngine.calculateTypography(
            availableHeightDp = availableHeightDp,
            availableWidthDp = availableWidthDp,
            hasSurahHeader = true,
            hasBismillah = false, // In Hafs, Bismillah is Ayah 1
            ayahCount = 7,
            totalCharacterCount = 299,
            mushafLines = 7
        )

        // Must not blow up beyond max aesthetic limit (24sp)
        assertTrue("Font size should not exceed 24sp for short pages", metrics.fontSizeSp <= 24f)
        assertTrue("Line height should not exceed 50sp", metrics.lineHeightSp <= 50f)
    }

    @Test
    fun testPageTypographyEngine_page16FillsScreenAndHasLargeFont() {
        // Page 16 (user screenshot): 1,123 characters on tall screen (H=800dp, W=360dp)
        val availableHeightDp = 800f
        val availableWidthDp = 360f

        val metrics = PageTypographyEngine.calculateTypography(
            availableHeightDp = availableHeightDp,
            availableWidthDp = availableWidthDp,
            hasSurahHeader = false,
            hasBismillah = false,
            ayahCount = 4,
            totalCharacterCount = 1123
        )

        // Must be large and easily readable (not tiny!)
        assertTrue("Font size on tall screen must be large and readable (>= 20sp), was ${metrics.fontSizeSp}", metrics.fontSizeSp >= 20f)
        assertTrue("Line height must be proportional (>= 38sp)", metrics.lineHeightSp >= 38f)

        // Must fill the vertical space (no huge blank dead zones)
        val nonTextHeight = 26f + 18f + (metrics.contentPaddingDp * 2)
        val textHeight = metrics.estimatedLines * metrics.lineHeightSp
        val totalHeight = nonTextHeight + textHeight
        val availableTextHeight = availableHeightDp - nonTextHeight

        val fillRatio = textHeight / availableTextHeight
        assertTrue("Fill ratio must utilize at least 95% of available text height, was $fillRatio", fillRatio >= 0.95f)
        assertTrue("Total height must strictly fit within screen height ($availableHeightDp dp), was $totalHeight", totalHeight <= availableHeightDp + 1f)
    }

    @Test
    fun testPageTypographyEngine_page8ExactFitNeverClippedByBottomBar() {
        // Page 8: 9 Ayahs, 1,197 characters on standard screen (H=700dp, W=340dp)
        val availableHeightDp = 700f
        val availableWidthDp = 340f

        val metrics = PageTypographyEngine.calculateTypography(
            availableHeightDp = availableHeightDp,
            availableWidthDp = availableWidthDp,
            hasSurahHeader = false,
            hasBismillah = false,
            ayahCount = 9,
            totalCharacterCount = 1197
        )

        assertTrue("Font size should be comfortably large (>= 18sp), was ${metrics.fontSizeSp}", metrics.fontSizeSp >= 18f)

        val nonTextHeight = 26f + 18f + (metrics.contentPaddingDp * 2)
        val totalHeight = nonTextHeight + (metrics.estimatedLines * metrics.lineHeightSp)

        assertTrue(
            "Page 8 total height ($totalHeight dp) must strictly fit within available screen height ($availableHeightDp dp)",
            totalHeight <= availableHeightDp + 1f
        )
    }

    @Test
    fun testPageTypographyEngine_densestPage552FitsWithoutOverflow() {
        // Page 552: 1,517 characters (densest page in the entire Quran)
        val availableHeightDp = 700f
        val availableWidthDp = 340f

        val metrics = PageTypographyEngine.calculateTypography(
            availableHeightDp = availableHeightDp,
            availableWidthDp = availableWidthDp,
            hasSurahHeader = false,
            hasBismillah = false,
            ayahCount = 12,
            totalCharacterCount = 1517
        )

        assertTrue("Font size on densest page should be readable (>= 16sp), was ${metrics.fontSizeSp}", metrics.fontSizeSp >= 16f)

        val nonTextHeight = 26f + 18f + (metrics.contentPaddingDp * 2)
        val totalHeight = nonTextHeight + (metrics.estimatedLines * metrics.lineHeightSp)

        assertTrue(
            "Densest Page 552 ($totalHeight dp) must fit within screen height ($availableHeightDp dp)",
            totalHeight <= availableHeightDp + 1f
        )
    }

    @Test
    fun testPageTypographyEngine_smallScreenHeightAdaptsSafely() {
        // Compact phone height (e.g. 540dp, width 320dp)
        val compactHeightDp = 540f
        val availableWidthDp = 320f

        val metrics = PageTypographyEngine.calculateTypography(
            availableHeightDp = compactHeightDp,
            availableWidthDp = availableWidthDp,
            hasSurahHeader = false,
            hasBismillah = false,
            ayahCount = 9,
            totalCharacterCount = 1197
        )

        assertTrue("Font size on small screen should adapt (>= 15sp), was ${metrics.fontSizeSp}", metrics.fontSizeSp >= 15f)
        val nonTextHeight = 26f + 18f + (metrics.contentPaddingDp * 2)
        val totalHeight = nonTextHeight + (metrics.estimatedLines * metrics.lineHeightSp)
        assertTrue("Total height must fit compact screen ($compactHeightDp dp)", totalHeight <= compactHeightDp + 1f)
    }

    @Test
    fun testPageTypographyEngine_multiHeaderPageFitsWithinHeight() {
        // Page 604: 3 Surahs (Al-Ikhlas, Al-Falaq, An-Nas) -> 3 headers + 3 bismillahs
        val availableHeightDp = 700f
        val availableWidthDp = 380f

        val metrics = PageTypographyEngine.calculateTypography(
            availableHeightDp = availableHeightDp,
            availableWidthDp = availableWidthDp,
            hasSurahHeader = true,
            hasBismillah = true,
            ayahCount = 15,
            totalCharacterCount = 445,
            headerCount = 3,
            bismillahCount = 3
        )

        val headersFootprint = (3 * 26f) + (3 * 20f) + 26f + 18f + (6 * 2f)
        val textFootprint = metrics.estimatedLines * metrics.lineHeightSp
        val totalFootprint = headersFootprint + textFootprint + (metrics.contentPaddingDp * 2)

        assertTrue(
            "Page 604 with 3 headers ($totalFootprint dp) must fit within available height ($availableHeightDp dp)",
            totalFootprint <= availableHeightDp + 1f
        )
    }

    // =========================================================================
    // 3. Immersive Mode Controller Tests (State Machine)
    // =========================================================================

    @Test
    fun testImmersiveModeController_toggleAndAutoHide() = runBlocking {
        // Use a short 60ms timeout for deterministic test execution
        val controller = ImmersiveModeController(
            coroutineScope = this,
            autoHideTimeoutMs = 60L
        )

        // Initially hidden for immersive reading
        assertFalse("Controls should be initially hidden", controller.isControlsVisible.value)

        // Toggle on tap -> shows controls and schedules auto-hide
        controller.toggle()
        assertTrue("Controls should be visible after toggle", controller.isControlsVisible.value)

        // Wait 20ms (not yet expired)
        delay(20L)
        assertTrue("Controls should still be visible after 20ms", controller.isControlsVisible.value)

        // Wait for remaining timeout + margin (expired)
        delay(60L)
        assertFalse("Controls should automatically hide after timeout", controller.isControlsVisible.value)

        // Explicit hide cancels immediately
        controller.showTemporarily()
        assertTrue("Controls should be visible", controller.isControlsVisible.value)
        controller.hide()
        assertFalse("Controls should hide immediately upon hide()", controller.isControlsVisible.value)
    }
}
