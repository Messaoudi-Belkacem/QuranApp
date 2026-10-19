package com.example.quranapp.presentation.screen.page

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.presentation.ui.theme.uthmaniFont
import com.example.quranapp.util.ArabicNumeralFormatter.convertToArabicNumerals
import kotlinx.coroutines.launch

// ========================================
// Constants
// ========================================

private const val ANIMATION_DURATION_STANDARD = 300
private const val ANIMATION_DURATION_FAST = 200
private const val ANIMATION_DURATION_VERY_FAST = 150
private const val SURAH_TAWBAH_ID = 9

// ========================================
// Main Screen
// ========================================

/**
 * Screen for reading a Quran page in full-height, non-scrollable Mushaf mode.
 *
 * Features:
 * - Natural Arabic RTL horizontal swiping between pages (1..604).
 * - Dynamic, immersive top app bar that appears on screen tap and auto-hides.
 * - Dynamic typography scaling ensuring the full page text fits without scrolling.
 * - Multi-page caching for instant, zero-flicker page transitions.
 *
 * @param pageNumber The initial Quran page number to display (1-604)
 * @param viewModel ViewModel managing the screen state
 * @param onBackClick Callback when back button is pressed
 */
@Composable
fun PageReadingScreen(
    pageNumber: Int,
    viewModel: PageReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    Log.d("PageReading", "Entering PageReadingScreen with pageNumber=$pageNumber")

    val initialPageIndex = remember(pageNumber) {
        PageNavigationPolicy.pageNumberToPageIndex(pageNumber)
    }
    val pagerState = rememberPagerState(initialPage = initialPageIndex) {
        PageNavigationPolicy.MAX_PAGE
    }

    val coroutineScope = rememberCoroutineScope()
    val immersiveController = remember {
        ImmersiveModeController(coroutineScope, autoHideTimeoutMs = 3500L)
    }
    val isControlsVisible by immersiveController.isControlsVisible.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Sync pager state with ViewModel page loader
    LaunchedEffect(pagerState.currentPage) {
        val currentMushafPage = PageNavigationPolicy.pageIndexToPageNumber(pagerState.currentPage)
        Log.d("PageReading", "Pager scrolled to page $currentMushafPage")
        viewModel.loadPage(currentMushafPage)
    }

    // Dismiss top bar immediately when swipe gesture begins
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            immersiveController.hide()
        }
    }

    // Handle external pageNumber parameter changes
    LaunchedEffect(pageNumber) {
        val targetIndex = PageNavigationPolicy.pageNumberToPageIndex(pageNumber)
        if (pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Full-screen Horizontal Pager in RTL layout direction
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val currentMushafPage = PageNavigationPolicy.pageIndexToPageNumber(pageIndex)
                    val groups = uiState.pagesCache[currentMushafPage]
                        ?: if (currentMushafPage == uiState.pageNumber) uiState.surahGroups else null

                    if (groups != null && groups.isNotEmpty()) {
                        PageMushafView(
                            pageNumber = currentMushafPage,
                            surahGroups = groups,
                            selectedAyahId = uiState.selectedAyahId,
                            selectedAyahSurahId = uiState.selectedAyahSurahId,
                            onAyahClick = { ayahId, surahId ->
                                viewModel.selectAyah(ayahId, surahId)
                            },
                            onScreenTap = {
                                immersiveController.toggle()
                                if (uiState.selectedAyahId != null) {
                                    viewModel.selectAyah(null, null)
                                }
                            }
                        )
                    } else if (uiState.errorMessage != null && currentMushafPage == uiState.pageNumber) {
                        PageErrorContent(
                            errorMessage = uiState.errorMessage!!,
                            onRetry = { viewModel.loadPage(currentMushafPage) }
                        )
                    } else {
                        PageLoadingContent()
                    }
                }
            }

            // 2. Dynamic, Immersive Top App Bar (Overlaid at top)
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(ANIMATION_DURATION_FAST)),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(ANIMATION_DURATION_FAST)),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                val currentMushafPage = PageNavigationPolicy.pageIndexToPageNumber(pagerState.currentPage)
                val currentSurah = uiState.pagesCache[currentMushafPage]?.firstOrNull()?.surah
                    ?: uiState.surahGroups.firstOrNull()?.surah

                PageDynamicTopBar(
                    pageNumber = currentMushafPage,
                    totalPages = PageNavigationPolicy.MAX_PAGE,
                    surahName = currentSurah?.nameEnglish,
                    onBackClick = onBackClick,
                    onPreviousPage = {
                        if (PageNavigationPolicy.hasPrevious(currentMushafPage)) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    onNextPage = {
                        if (PageNavigationPolicy.hasNext(currentMushafPage)) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    hasPrevious = PageNavigationPolicy.hasPrevious(currentMushafPage),
                    hasNext = PageNavigationPolicy.hasNext(currentMushafPage)
                )
            }

            // 3. Ayah Actions Tooltip (Overlaid near bottom when an ayah is selected)
            AnimatedVisibility(
                visible = uiState.selectedAyahId != null && uiState.selectedAyahId != -1,
                enter = fadeIn(animationSpec = tween(ANIMATION_DURATION_FAST)) + scaleIn(
                    animationSpec = tween(ANIMATION_DURATION_FAST),
                    initialScale = 0.85f
                ),
                exit = fadeOut(animationSpec = tween(ANIMATION_DURATION_VERY_FAST)) + scaleOut(
                    animationSpec = tween(ANIMATION_DURATION_VERY_FAST),
                    targetScale = 0.85f
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                if (uiState.selectedAyahId != null && uiState.selectedAyahId != -1) {
                    AyahActionsTooltip(
                        ayahId = uiState.selectedAyahId!!,
                        onDismiss = { viewModel.selectAyah(null, null) }
                    )
                }
            }
        }
    }
}

// ========================================
// Dynamic Top App Bar
// ========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageDynamicTopBar(
    pageNumber: Int,
    totalPages: Int,
    surahName: String?,
    onBackClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Page $pageNumber",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (surahName != null) "$surahName • of $totalPages pages" else "of $totalPages pages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onPreviousPage,
                        enabled = hasPrevious
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                            contentDescription = "Previous page",
                            tint = if (hasPrevious)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    IconButton(
                        onClick = onNextPage,
                        enabled = hasNext
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = "Next page",
                            tint = if (hasNext)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

// ========================================
// Page Mushaf View (Fixed Height, Non-scrollable)
// ========================================

@Composable
private fun PageMushafView(
    pageNumber: Int,
    surahGroups: List<SurahAyahGroup>,
    selectedAyahId: Int?,
    selectedAyahSurahId: Int?,
    onAyahClick: (Int?, Int?) -> Unit,
    onScreenTap: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onScreenTap() }
                )
            }
    ) {
        val starts = surahGroups.filter { it.ayahs.firstOrNull()?.id == 1 }
        val hasSurahHeader = starts.isNotEmpty()
        val headerCount = starts.size
        val bismillahCount = starts.count {
            it.surah.id != SURAH_TAWBAH_ID && !(it.surah.id == 1 && it.ayahs.firstOrNull()?.id == 1)
        }
        val totalAyahCount = surahGroups.sumOf { it.ayahs.size }
        val totalCharCount = surahGroups.sumOf { group -> group.ayahs.sumOf { it.text.length } }

        // Extract exact line count from official Madinah Mushaf coordinates
        val allAyahs = surahGroups.flatMap { it.ayahs }
        val mushafLines = if (allAyahs.isNotEmpty()) {
            (allAyahs.maxOf { it.lineEnd } - allAyahs.minOf { it.lineStart } + 1).coerceIn(5, 15)
        } else {
            15
        }

        val baseMetrics = remember(maxHeight, maxWidth, pageNumber, surahGroups) {
            PageTypographyEngine.calculateTypography(
                availableHeightDp = maxHeight.value,
                availableWidthDp = maxWidth.value,
                hasSurahHeader = hasSurahHeader,
                hasBismillah = bismillahCount > 0,
                ayahCount = totalAyahCount,
                totalCharacterCount = totalCharCount,
                headerCount = headerCount,
                bismillahCount = bismillahCount,
                mushafLines = mushafLines
            )
        }

        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()

        // Pre-flight text measurement: verifies that text height strictly fits without vertical overflow
        val metrics = remember(baseMetrics, maxWidth, maxHeight, surahGroups) {
            val horizontalPaddingPx = with(density) { (baseMetrics.contentPaddingDp * 2).dp.roundToPx() }
            val maxWidthPx = (with(density) { maxWidth.roundToPx() } - horizontalPaddingPx).coerceAtLeast(100)
            val nonTextDp = (baseMetrics.contentPaddingDp * 2) + 64f + (if (hasSurahHeader) 34f else 0f) + (if (bismillahCount > 0) 26f else 0f)
            val availableTextHeightPx = with(density) { (maxHeight.value - nonTextDp).coerceAtLeast(100f).dp.roundToPx() }

            val sampleText = buildAnnotatedString {
                surahGroups.forEach { group ->
                    group.ayahs.forEach { append(" ${it.text}") }
                }
            }

            var adjustedFontSize = baseMetrics.fontSizeSp
            var adjustedLineHeight = baseMetrics.lineHeightSp
            var iterations = 0

            while (iterations < 5 && adjustedFontSize > PageTypographyEngine.MIN_FONT_SIZE_SP) {
                val measured = textMeasurer.measure(
                    text = sampleText,
                    style = TextStyle(
                        fontFamily = uthmaniFont,
                        fontSize = adjustedFontSize.sp,
                        lineHeight = adjustedLineHeight.sp,
                        textAlign = TextAlign.Justify,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None
                        )
                    ),
                    constraints = Constraints(maxWidth = maxWidthPx)
                )

                if (measured.size.height <= availableTextHeightPx) {
                    break
                }
                adjustedFontSize -= 0.5f
                adjustedLineHeight = (adjustedFontSize * 1.9f).coerceIn(PageTypographyEngine.MIN_LINE_HEIGHT_SP, PageTypographyEngine.MAX_LINE_HEIGHT_SP)
                iterations++
            }

            baseMetrics.copy(
                fontSizeSp = adjustedFontSize,
                lineHeightSp = adjustedLineHeight
            )
        }

        val primarySurah = surahGroups.firstOrNull()?.surah
        val currentJuzz = surahGroups.firstOrNull()?.ayahs?.firstOrNull()?.juzz ?: 1

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = metrics.contentPaddingDp.dp,
                    vertical = 4.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subtle Mushaf top info line (Juz & Surah)
            MushafTopInfoLine(
                surahArabic = primarySurah?.nameArabic ?: "",
                juzz = currentJuzz
            )

            // Middle section: Strictly bounded by weight(1f) Box, centered vertically
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    surahGroups.forEach { group ->
                        val surah = group.surah
                        val ayahs = group.ayahs
                        val isSurahStart = ayahs.firstOrNull()?.id == 1

                        if (isSurahStart) {
                            CompactSurahHeader(
                                surah = surah,
                                isMultiHeader = headerCount > 1
                            )

                            if (surah.id != SURAH_TAWBAH_ID && !(surah.id == 1 && ayahs.firstOrNull()?.id == 1)) {
                                Spacer(modifier = Modifier.height(metrics.headerSpacingDp.dp))
                                CompactBismillah(isMultiHeader = headerCount > 1)
                            }

                            Spacer(modifier = Modifier.height(metrics.headerSpacingDp.dp))
                        }

                        PageMushafContinuousText(
                            ayahs = ayahs,
                            selectedAyahId = if (selectedAyahSurahId == surah.id) selectedAyahId else null,
                            fontSizeSp = metrics.fontSizeSp,
                            lineHeightSp = metrics.lineHeightSp,
                            onAyahClick = { ayahId ->
                                if (ayahId == -1) {
                                    onAyahClick(null, null)
                                } else {
                                    onAyahClick(ayahId, surah.id)
                                }
                            }
                        )
                    }
                }
            }

            // Mushaf bottom footer: page number (permanently docked at bottom without overlap)
            PageFooter(pageNumber = pageNumber)
        }
    }
}

// ========================================
// Compact Surah Header
// ========================================

@Composable
private fun CompactSurahHeader(
    surah: Surah,
    isMultiHeader: Boolean = false
) {
    val heightDp = if (isMultiHeader) 32.dp else 40.dp
    val textFontSize = if (isMultiHeader) 17.sp else 19.sp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "۞",
                style = TextStyle(
                    fontFamily = uthmaniFont,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "سُورَةُ ${surah.nameArabic}",
                style = TextStyle(
                    fontFamily = uthmaniFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = textFontSize,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = "۞",
                style = TextStyle(
                    fontFamily = uthmaniFont,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

// ========================================
// Compact Bismillah
// ========================================

@Composable
private fun CompactBismillah(isMultiHeader: Boolean = false) {
    val fontSize = if (isMultiHeader) 18.sp else 21.sp
    val lineHeight = if (isMultiHeader) 26.sp else 30.sp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isMultiHeader) 2.dp else 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            style = TextStyle(
                fontFamily = uthmaniFont,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                lineHeight = lineHeight,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// ========================================
// Mushaf Top Info Line (Header)
// ========================================

@Composable
private fun MushafTopInfoLine(
    surahArabic: String,
    juzz: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "الجزء $juzz",
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = uthmaniFont),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Text(
            text = "سورة $surahArabic",
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = uthmaniFont),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// ========================================
// Continuous Mushaf Text
// ========================================

@Composable
@Suppress("DEPRECATION")
private fun PageMushafContinuousText(
    ayahs: List<Ayah>,
    selectedAyahId: Int?,
    fontSizeSp: Float,
    lineHeightSp: Float,
    onAyahClick: (Int) -> Unit
) {
    val annotatedText = remember(ayahs, selectedAyahId, fontSizeSp) {
        buildPageMushafAnnotatedString(
            ayahs = ayahs,
            selectedAyahId = selectedAyahId,
            fontSizeSp = fontSizeSp
        )
    }

    ClickableText(
        text = annotatedText,
        style = TextStyle(
            textAlign = TextAlign.Justify,
            lineHeight = lineHeightSp.sp,
            fontSize = fontSizeSp.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None
            )
        ),
        modifier = Modifier.fillMaxWidth(),
        onClick = { offset ->
            annotatedText
                .getStringAnnotations("AYAH", offset, offset)
                .firstOrNull()
                ?.let { annotation ->
                    val ayahId = annotation.item.toIntOrNull()
                    if (ayahId != null) {
                        if (selectedAyahId == ayahId) {
                            onAyahClick(-1)
                        } else {
                            onAyahClick(ayahId)
                        }
                    }
                }
        }
    )
}

/**
 * Builds an annotated string for Mushaf-style continuous text display.
 */
private fun buildPageMushafAnnotatedString(
    ayahs: List<Ayah>,
    selectedAyahId: Int?,
    fontSizeSp: Float
) = buildAnnotatedString {
    ayahs.forEach { ayah ->
        pushStringAnnotation(
            tag = "AYAH",
            annotation = ayah.id.toString()
        )

        val isSelected = ayah.id == selectedAyahId

        withStyle(
            style = SpanStyle(
                fontSize = fontSizeSp.sp,
                letterSpacing = 0.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = uthmaniFont,
                color = if (isSelected) {
                    Color(0xFF1976D2)
                } else {
                    Color.Unspecified
                },
                background = if (isSelected) {
                    Color(0xFFE3F2FD)
                } else {
                    Color.Transparent
                }
            )
        ) {
            append(" ${ayah.text}")
        }
        pop()
    }
}

// ========================================
// Ayah Actions Tooltip
// ========================================

@Composable
private fun AyahActionsTooltip(
    ayahId: Int,
    onDismiss: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clip(CircleShape)
            ) {
                Text(
                    text = convertToArabicNumerals(ayahId),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            IconButton(
                onClick = { /* Handle bookmark */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { /* Handle copy */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { /* Handle share */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ========================================
// Page Footer
// ========================================

@Composable
private fun PageFooter(pageNumber: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 1.dp
        ) {
            Text(
                text = "— $pageNumber —",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ========================================
// State Screens (Loading, Error, Empty)
// ========================================

@Composable
private fun PageLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )

            Text(
                text = "Loading Page...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PageErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Failed to Load Page",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun PageEmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = "No content",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Text(
                text = "No Ayahs on this Page",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "This page doesn't have any ayahs loaded yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
