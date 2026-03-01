package com.example.quranapp.presentation.screen.page

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.quranapp.data.database.entities.Ayah
import com.example.quranapp.data.database.entities.Surah
import com.example.quranapp.presentation.ui.theme.uthmaniFont
import kotlinx.coroutines.launch

// ========================================
// Constants
// ========================================

private const val SCROLL_TO_TOP_THRESHOLD = 3
private const val ANIMATION_DURATION_STANDARD = 300
private const val ANIMATION_DURATION_FAST = 200
private const val ANIMATION_DURATION_VERY_FAST = 150
private const val SURAH_TAWBAH_ID = 9

// ========================================
// Main Screen
// ========================================

/**
 * Screen for reading a Quran page. Displays all ayahs on the given page number,
 * grouped by their surah in Mushaf-style continuous text.
 *
 * @param pageNumber The Quran page number to display (1-604)
 * @param viewModel ViewModel managing the screen state
 * @param onBackClick Callback when back button is pressed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageReadingScreen(
    pageNumber: Int,
    viewModel: PageReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    Log.d("PageReading", "Entering PageReadingScreen with pageNumber=$pageNumber")

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showScrollToTop by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pageNumber) {
        Log.d("PageReading", "Requesting loadPage($pageNumber)")
        viewModel.loadPage(pageNumber)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                showScrollToTop = index > SCROLL_TO_TOP_THRESHOLD
            }
    }

    Scaffold(
        topBar = {
            PageTopAppBar(
                pageNumber = uiState.pageNumber,
                totalPages = uiState.totalPages,
                onBackClick = onBackClick,
                onPreviousPage = { viewModel.previousPage() },
                onNextPage = { viewModel.nextPage() },
                hasPrevious = uiState.pageNumber > 1,
                hasNext = uiState.pageNumber < uiState.totalPages
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = scaleIn(animationSpec = tween(ANIMATION_DURATION_STANDARD)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(ANIMATION_DURATION_STANDARD)) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    PageLoadingContent()
                }

                uiState.errorMessage != null -> {
                    PageErrorContent(
                        errorMessage = uiState.errorMessage!!,
                        onRetry = { viewModel.loadPage(uiState.pageNumber) }
                    )
                }

                uiState.surahGroups.isNotEmpty() -> {
                    PageContent(
                        pageNumber = uiState.pageNumber,
                        surahGroups = uiState.surahGroups,
                        selectedAyahId = uiState.selectedAyahId,
                        selectedAyahSurahId = uiState.selectedAyahSurahId,
                        onAyahClick = { ayahId, surahId -> viewModel.selectAyah(ayahId, surahId) },
                        listState = listState
                    )
                }

                else -> {
                    PageEmptyContent()
                }
            }
        }
    }
}

// ========================================
// Top App Bar
// ========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageTopAppBar(
    pageNumber: Int,
    totalPages: Int,
    onBackClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
) {
    TopAppBar(
        title = {
            AnimatedContent(
                targetState = pageNumber,
                transitionSpec = {
                    fadeIn(animationSpec = tween(ANIMATION_DURATION_STANDARD)) togetherWith
                            fadeOut(animationSpec = tween(ANIMATION_DURATION_STANDARD))
                },
                label = "page_title"
            ) { targetPage ->
                Column {
                    Text(
                        text = "Page $targetPage",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "of $totalPages pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
            // Previous page button
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

            // Next page button
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
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ========================================
// Page Content
// ========================================

@Composable
private fun PageContent(
    pageNumber: Int,
    surahGroups: List<SurahAyahGroup>,
    selectedAyahId: Int?,
    selectedAyahSurahId: Int?,
    onAyahClick: (Int?, Int?) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Render each surah group
        surahGroups.forEach { group ->
            val surah = group.surah
            val ayahs = group.ayahs

            // Show surah name header if the first ayah is ayah 1 (surah starts on this page)
            if (ayahs.first().id == 1) {
                item(key = "surah_header_${surah.id}") {
                    SurahNameHeader(surah = surah)
                }

                // Bismillah for surahs that start on this page (except At-Tawbah)
                if (surah.id != SURAH_TAWBAH_ID) {
                    item(key = "bismillah_${surah.id}") {
                        BismillahCard()
                    }
                }
            }

            // Mushaf-style text for this surah's ayahs on this page
            item(key = "mushaf_${surah.id}_page_$pageNumber") {
                PageMushafTextCard(
                    ayahs = ayahs,
                    selectedAyahId = if (selectedAyahSurahId == surah.id) selectedAyahId else null,
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

        // Page number footer
        item(key = "page_footer") {
            PageFooter(pageNumber = pageNumber)
        }
    }
}

// ========================================
// Surah Name Header (when surah starts on this page)
// ========================================

@Composable
private fun SurahNameHeader(surah: Surah) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = uthmaniFont
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${surah.nameEnglish} • ${surah.totalVerses} Ayahs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ========================================
// Bismillah Card
// ========================================

@Composable
private fun BismillahCard() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = uthmaniFont
                    ),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 28.sp,
                    lineHeight = 42.sp
                )
            }
        }
    }
}

// ========================================
// Mushaf Text Card for Page
// ========================================

@Composable
@Suppress("DEPRECATION")
private fun PageMushafTextCard(
    ayahs: List<Ayah>,
    selectedAyahId: Int? = null,
    onAyahClick: (Int) -> Unit = {},
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val annotatedText = remember(ayahs, selectedAyahId) {
                        buildPageMushafAnnotatedString(ayahs, selectedAyahId)
                    }

                    ClickableText(
                        text = annotatedText,
                        style = TextStyle(
                            textAlign = TextAlign.Justify,
                            lineHeight = 40.sp,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
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
            }

            // Ayah actions tooltip — keep LTR so icon order stays conventional
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                AnimatedVisibility(
                    visible = selectedAyahId != null && selectedAyahId != -1,
                    enter = fadeIn(animationSpec = tween(ANIMATION_DURATION_FAST)) + scaleIn(
                        animationSpec = tween(ANIMATION_DURATION_FAST),
                        initialScale = 0.8f
                    ),
                    exit = fadeOut(animationSpec = tween(ANIMATION_DURATION_VERY_FAST)) + scaleOut(
                        animationSpec = tween(ANIMATION_DURATION_VERY_FAST),
                        targetScale = 0.8f
                    ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    if (selectedAyahId != null && selectedAyahId != -1) {
                        AyahActionsTooltip(
                            ayahId = selectedAyahId,
                            onDismiss = { onAyahClick(-1) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds an annotated string for Mushaf-style text display.
 */
private fun buildPageMushafAnnotatedString(
    ayahs: List<Ayah>,
    selectedAyahId: Int?,
) = buildAnnotatedString {
    ayahs.forEach { ayah ->
        pushStringAnnotation(
            tag = "AYAH",
            annotation = ayah.id.toString()
        )

        val isSelected = ayah.id == selectedAyahId

        withStyle(
            style = SpanStyle(
                fontSize = 26.sp,
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
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Text(
                text = "— $pageNumber —",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ========================================
// Helper Functions
// ========================================

private fun convertToArabicNumerals(number: Int): String {
    val arabicNumerals = mapOf(
        '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
        '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
    )
    return number.toString().map { arabicNumerals[it] ?: it }.joinToString("")
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


