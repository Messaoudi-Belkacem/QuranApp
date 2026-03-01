package com.example.quranapp.presentation.screen.surah

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
private const val SURAH_TAWBAH_ID = 9 // Surah At-Tawbah doesn't have Bismillah

// ========================================
// Main Screen
// ========================================

/**
 * Main screen for reading a Surah with its ayahs.
 * Supports two viewing modes: LIST (card-based) and MUSHAF (continuous text).
 * Includes search functionality and scroll-to-top FAB.
 *
 * @param surahId The ID of the surah to display (1-114)
 * @param viewModel ViewModel managing the screen state
 * @param onBackClick Callback when back button is pressed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReadingScreen(
    surahId: Int,
    viewModel: SurahReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    Log.d("SurahReading", "Entering SurahReadingScreen with surahId=$surahId")

    // State management
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showScrollToTop by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Load surah when screen opens or surahId changes
    LaunchedEffect(surahId) {
        Log.d("SurahReading", "Requesting loadSurah($surahId)")
        viewModel.loadSurah(surahId)
    }

    // Show/hide scroll-to-top FAB based on scroll position
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                showScrollToTop = index > SCROLL_TO_TOP_THRESHOLD
            }
    }

    Scaffold(
        topBar = {
            SurahTopAppBar(
                surah = uiState.currentSurah,
                onBackClick = onBackClick,
                isLoading = uiState.isLoading,
                onSearchClick = {
                    isSearchVisible = !isSearchVisible
                }, // Fix: Toggle search visibility
                isSearchVisible = isSearchVisible,
                viewingMode = uiState.viewingMode,
                onViewingModeToggle = { viewModel.toggleViewingMode() }
            )
        },
        floatingActionButton = {
            // Scroll to top FAB
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
            // Content based on loading state
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }

                uiState.errorMessage != null -> {
                    ErrorContent(
                        errorMessage = uiState.errorMessage!!,
                        onRetry = { viewModel.loadSurah(surahId) }
                    )
                }

                uiState.currentSurah != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Bar (shown/hidden with animation)
                        AnimatedVisibility(
                            visible = isSearchVisible,
                            enter = expandVertically(
                                animationSpec = tween(
                                    ANIMATION_DURATION_STANDARD
                                )
                            ) + fadeIn(),
                            exit = shrinkVertically(
                                animationSpec = tween(
                                    ANIMATION_DURATION_STANDARD
                                )
                            ) + fadeOut()
                        ) {
                            SearchBar(
                                searchQuery = uiState.searchQuery,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onClearSearch = { viewModel.clearSearch() }
                            )
                        }

                        // Main content area
                        if (uiState.filteredAyahs.isEmpty() && uiState.searchQuery.isNotBlank()) {
                            EmptySearchContent()
                        } else {
                            // Switch between viewing modes
                            when (uiState.viewingMode) {
                                ViewingMode.LIST -> {
                                    ListViewContent(
                                        surah = uiState.currentSurah!!,
                                        ayahs = uiState.filteredAyahs,
                                        listState = listState,
                                        searchQuery = uiState.searchQuery
                                    )
                                }

                                ViewingMode.MUSHAF -> {
                                    MushafViewContent(
                                        surah = uiState.currentSurah!!,
                                        ayahs = uiState.filteredAyahs,
                                        listState = listState,
                                        searchQuery = uiState.searchQuery,
                                        selectedAyahId = uiState.selectedAyahId,
                                        onAyahClick = { ayahId -> viewModel.selectAyah(ayahId) }
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    EmptyContent()
                }
            }
        }
    }
}

// ========================================
// Top App Bar
// ========================================

/**
 * Top app bar for the Surah Reading screen.
 * Displays surah name, navigation button, view mode toggle, and search button.
 *
 * @param surah The current surah being displayed
 * @param onBackClick Callback for back navigation
 * @param isLoading Whether data is currently loading
 * @param onSearchClick Callback to toggle search visibility
 * @param isSearchVisible Whether search bar is currently visible
 * @param viewingMode Current viewing mode (LIST or MUSHAF)
 * @param onViewingModeToggle Callback to toggle viewing mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahTopAppBar(
    surah: Surah?,
    onBackClick: () -> Unit,
    isLoading: Boolean,
    onSearchClick: () -> Unit,
    isSearchVisible: Boolean,
    viewingMode: ViewingMode = ViewingMode.LIST,
    onViewingModeToggle: () -> Unit = {},
) {
    TopAppBar(
        title = {
            // Animated title that shows surah name or loading state
            AnimatedContent(
                targetState = surah,
                transitionSpec = {
                    fadeIn(animationSpec = tween(ANIMATION_DURATION_STANDARD)) togetherWith
                            fadeOut(animationSpec = tween(ANIMATION_DURATION_STANDARD))
                },
                label = "title"
            ) { targetSurah ->
                if (targetSurah != null) {
                    Column {
                        Text(
                            text = targetSurah.nameArabic,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = targetSurah.nameEnglish,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (isLoading) {
                    Text("Loading...")
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
            // Viewing mode toggle button
            IconButton(onClick = onViewingModeToggle) {
                Icon(
                    imageVector = if (viewingMode == ViewingMode.MUSHAF)
                        Icons.AutoMirrored.Filled.ViewList
                    else
                        Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = if (viewingMode == ViewingMode.MUSHAF)
                        "Switch to List View"
                    else
                        "Switch to Mushaf View"
                )
            }

            // Search button
            IconButton(onClick = onSearchClick) {
                AnimatedContent(
                    targetState = isSearchVisible,
                    transitionSpec = {
                        scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                    },
                    label = "search_icon"
                ) { searchVisible ->
                    Icon(
                        imageVector = if (searchVisible) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (searchVisible) "Close search" else "Search"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ========================================
// Search Bar
// ========================================

/**
 * Search bar for filtering ayahs by text or number.
 *
 * @param searchQuery Current search query text
 * @param onSearchQueryChange Callback when search query changes
 * @param onClearSearch Callback to clear the search
 */
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Search ayahs...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = onClearSearch) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ========================================
// Content Views
// ========================================

/**
 * List view content - displays ayahs in individual cards.
 * Each ayah is shown in a separate card with actions, Arabic text, and translation.
 *
 * @param surah The current surah
 * @param ayahs List of ayahs to display
 * @param listState State for managing scroll position
 * @param searchQuery Current search query for highlighting
 */
@Composable
private fun ListViewContent(
    surah: Surah,
    ayahs: List<Ayah>,
    listState: LazyListState,
    searchQuery: String,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Surah Header
        item(key = "header") {
            SurahHeaderCard(surah = surah)
        }

        // Bismillah (except for Surah At-Tawbah and when searching)
        if (surah.id != SURAH_TAWBAH_ID && searchQuery.isBlank()) {
            item(key = "bismillah") {
                BismillahCard()
            }
        }

        // Ayahs with optimized lazy loading
        items(
            items = ayahs,
            key = { ayah -> "ayah_${ayah.id}" }
        ) { ayah ->
            AyahCard(
                ayah = ayah,
                highlight = searchQuery.isNotBlank()
            )
        }

        // Bottom spacer
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Mushaf view content - displays ayahs as continuous flowing text.
 * Text resembles a traditional Quran page with clickable ayahs.
 *
 * @param surah The current surah
 * @param ayahs List of ayahs to display
 * @param listState State for managing scroll position
 * @param searchQuery Current search query
 * @param selectedAyahId ID of the currently selected ayah
 * @param onAyahClick Callback when an ayah is clicked
 */
@Composable
private fun MushafViewContent(
    surah: Surah,
    ayahs: List<Ayah>,
    listState: LazyListState,
    searchQuery: String,
    selectedAyahId: Int? = null,
    onAyahClick: (Int) -> Unit = {},
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Surah Header
        item(key = "header") {
            SurahHeaderCard(surah = surah)
        }

        // Bismillah (except for Surah At-Tawbah and when searching)
        if (surah.id != SURAH_TAWBAH_ID && searchQuery.isBlank()) {
            item(key = "bismillah") {
                BismillahCard()
            }
        }

        // Mushaf-style continuous text
        item(key = "mushaf_text") {
            MushafTextCard(
                ayahs = ayahs,
                selectedAyahId = selectedAyahId,
                onAyahClick = onAyahClick
            )
        }

        // Bottom spacer
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ========================================
// Mushaf Text Components
// ========================================

/**
 * Card displaying ayahs in continuous text format (Mushaf style).
 * Ayahs are clickable and show a tooltip with actions when selected.
 *
 * @param ayahs List of ayahs to display
 * @param selectedAyahId ID of the currently selected ayah
 * @param onAyahClick Callback when an ayah is clicked
 */
@Composable
@Suppress("DEPRECATION")
private fun MushafTextCard(
    ayahs: List<Ayah>,
    selectedAyahId: Int? = null,
    onAyahClick: (Int) -> Unit = {},
) {
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
                // Build clickable annotated string with ayah markers
                val annotatedText = remember(ayahs, selectedAyahId) {
                    buildMushafAnnotatedString(ayahs, selectedAyahId)
                }

                // Display the clickable text
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
                        handleAyahClick(annotatedText, offset, selectedAyahId, onAyahClick)
                    }
                )
            }
        }

        // Show ayah actions as a hovering tooltip when an ayah is selected
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

/**
 * Builds an annotated string for Mushaf-style text display.
 * Each ayah is annotated for click detection and styled based on selection.
 */
private fun buildMushafAnnotatedString(
    ayahs: List<Ayah>,
    selectedAyahId: Int?,
) = buildAnnotatedString {
    ayahs.forEach { ayah ->
        // Add annotation for click detection
        pushStringAnnotation(
            tag = "AYAH",
            annotation = ayah.id.toString()
        )

        // Apply styling based on selection state
        val isSelected = ayah.id == selectedAyahId
        withStyle(
            style = SpanStyle(
                fontSize = 26.sp,
                letterSpacing = 0.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = uthmaniFont,
                color = if (isSelected) {
                    androidx.compose.ui.graphics.Color(0xFF1976D2)
                } else {
                    Color.Unspecified
                },
                background = if (isSelected) {
                    androidx.compose.ui.graphics.Color(0xFFE3F2FD)
                } else {
                    Color.Transparent
                }
            )
        ) {
            append(ayah.text)

            // Add ayah number marker in Arabic numerals
            withStyle(
                style = SpanStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = uthmaniFont,
                    color = androidx.compose.ui.graphics.Color(0xFF1976D2)
                )
            ) {
                append(" ${convertToArabicNumerals(ayah.id)}۝ ")
            }
        }

        pop()
    }
}

/**
 * Handles click events on the Mushaf text.
 * Detects which ayah was clicked and toggles its selection.
 */
private fun handleAyahClick(
    annotatedText: AnnotatedString,
    offset: Int,
    selectedAyahId: Int?,
    onAyahClick: (Int) -> Unit,
) {
    annotatedText
        .getStringAnnotations("AYAH", offset, offset)
        .firstOrNull()
        ?.let { annotation ->
            val ayahId = annotation.item.toIntOrNull()
            if (ayahId != null) {
                // Toggle selection
                if (selectedAyahId == ayahId) {
                    onAyahClick(-1) // Deselect
                } else {
                    onAyahClick(ayahId) // Select
                }
            }
        }
}

/**
 * Floating tooltip displaying ayah actions (bookmark, copy, share).
 * Appears when an ayah is selected in Mushaf mode.
 *
 * @param ayahId ID of the selected ayah
 * @param onDismiss Callback to dismiss the tooltip
 */
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
            // Ayah number indicator
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

            // Action buttons
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
// Helper Functions
// ========================================

/**
 * Converts a number to Arabic-Indic numerals.
 * Example: 123 -> ١٢٣
 */
private fun convertToArabicNumerals(number: Int): String {
    val arabicNumerals = mapOf(
        '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
        '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
    )
    return number.toString().map { arabicNumerals[it] ?: it }.joinToString("")
}

// ========================================
// Card Components
// ========================================

/**
 * Header card displaying surah information.
 * Shows Arabic name, transliteration, and metadata (ayah count, type, number).
 * Includes an animated gradient background.
 *
 * @param surah The surah to display
 */
@Composable
private fun SurahHeaderCard(
    surah: Surah,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    var isExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                onClick = { isExpanded = !isExpanded }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Background gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.1f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Arabic name
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = uthmaniFont
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                        animationSpec = tween(300)
                    ),
                    exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                        animationSpec = tween(300)
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // English name
                        Text(
                            text = surah.nameEnglish,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider
                        HorizontalDivider(
                            modifier = Modifier.width(80.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InfoChip(
                                icon = Icons.Default.Book,
                                label = "Ayahs",
                                value = surah.totalVerses.toString()
                            )

                            InfoChip(
                                icon = Icons.Default.LocationOn,
                                label = "Juzz",
                                value = surah.startJuzz.toString()
                            )

                            InfoChip(
                                icon = Icons.Default.Tag,
                                label = "No.",
                                value = surah.id.toString()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Small chip displaying an info item (icon + label + optional value).
 * Used in the surah header card.
 */
@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = 2.dp,
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Card displaying "Bismillah" (In the name of Allah).
 * Shown at the beginning of each surah except Surah At-Tawbah.
 */
@Composable
private fun BismillahCard() {
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

/**
 * Card displaying a single ayah in list view mode.
 * Includes ayah number, Arabic text, translation, and action buttons.
 *
 * @param ayah The ayah to display
 * @param highlight Whether to highlight the card (e.g., for search results)
 */
@Composable
private fun AyahCard(
    ayah: Ayah,
    highlight: Boolean = false,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (highlight) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (highlight) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Ayah number header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Text(
                        text = ayah.id.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { /* Handle bookmark */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { /* Handle copy */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { /* Handle share */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic text
            Text(
                text = ayah.text,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = uthmaniFont
                ),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 48.sp,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Translation placeholder
            Text(
                text = "Translation will be shown here",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

// ========================================
// State Screens (Loading, Error, Empty)
// ========================================

/**
 * Loading state screen with circular progress indicator.
 */
@Composable
private fun LoadingContent() {
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
                text = "Loading Surah...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state screen with retry button.
 *
 * @param errorMessage The error message to display
 * @param onRetry Callback when retry button is clicked
 */
@Composable
private fun ErrorContent(
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
                text = "Failed to Load Surah",
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

/**
 * Empty state screen when no surah is loaded.
 */
@Composable
private fun EmptyContent() {
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
                text = "No Ayahs Available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "This surah doesn't have any ayahs loaded yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Empty search results screen.
 */
@Composable
private fun EmptySearchContent() {
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
                imageVector = Icons.Default.SearchOff,
                contentDescription = "No search results",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Text(
                text = "No Ayahs Found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Try adjusting your search terms",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
