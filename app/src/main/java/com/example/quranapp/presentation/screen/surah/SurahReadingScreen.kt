package com.example.quranapp.presentation.screen.surah

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReadingScreen(
    surahId: Int,
    viewModel: SurahReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    Log.d("SurahReading", "Entering SurahReadingScreen with surahId=$surahId")

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showScrollToTop by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(surahId) {
        Log.d("SurahReading", "Requesting loadSurah($surahId)")
        viewModel.loadSurah(surahId)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                showScrollToTop = index > 3
            }
    }

    Scaffold(
        topBar = {
            SurahTopAppBar(
                surah = uiState.currentSurah,
                onBackClick = onBackClick,
                isLoading = uiState.isLoading,
                onSearchClick = { isSearchVisible = !isSearchVisible },
                isSearchVisible = isSearchVisible,
                viewingMode = uiState.viewingMode,
                onViewingModeToggle = { viewModel.toggleViewingMode() }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = scaleIn(animationSpec = tween(300)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(300)) + fadeOut()
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
                        // Search Bar
                        AnimatedVisibility(
                            visible = isSearchVisible,
                            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                        ) {
                            SearchBar(
                                searchQuery = uiState.searchQuery,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onClearSearch = { viewModel.clearSearch() }
                            )
                        }

                        // Content
                        if (uiState.filteredAyahs.isEmpty() && uiState.searchQuery.isNotBlank()) {
                            EmptySearchContent()
                        } else {
                            // Switch between viewing modes
                            when (uiState.viewingMode) {
                                ViewingMode.LIST -> {
                                    SurahContent(
                                        surah = uiState.currentSurah!!,
                                        ayahs = uiState.filteredAyahs,
                                        listState = listState,
                                        searchQuery = uiState.searchQuery
                                    )
                                }
                                ViewingMode.MUSHAF -> {
                                    SurahContentText(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahTopAppBar(
    surah: com.example.quranapp.data.database.entities.Surah?,
    onBackClick: () -> Unit,
    isLoading: Boolean,
    onSearchClick: () -> Unit,
    isSearchVisible: Boolean,
    viewingMode: ViewingMode = ViewingMode.LIST,
    onViewingModeToggle: () -> Unit = {}
) {
    TopAppBar(
        title = {
            AnimatedContent(
                targetState = surah,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "title"
            ) { targetSurah ->
                if (targetSurah != null) {
                    Column {
                        Text(
                            text = targetSurah.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = targetSurah.transliteration,
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
            // Viewing mode toggle
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

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
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
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
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

@Composable
private fun SurahContent(
    surah: Surah,
    ayahs: List<Ayah>,
    listState: LazyListState,
    searchQuery: String
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
        if (surah.id != 9 && searchQuery.isBlank()) {
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

@Composable
private fun SurahContentText(
    surah: Surah,
    ayahs: List<Ayah>,
    listState: LazyListState,
    searchQuery: String,
    selectedAyahId: Int? = null,
    onAyahClick: (Int) -> Unit = {}
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
        if (surah.id != 9 && searchQuery.isBlank()) {
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

@Composable
@Suppress("DEPRECATION")
private fun MushafTextCard(
    ayahs: List<Ayah>,
    selectedAyahId: Int? = null,
    onAyahClick: (Int) -> Unit = {}
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
            // Build annotated string with ayah annotations
            val annotatedText = remember(ayahs, selectedAyahId) {
                buildAnnotatedString {
                    ayahs.forEach { ayah ->
                        // Add annotation for this ayah
                        pushStringAnnotation(
                            tag = "AYAH",
                            annotation = ayah.id.toString()
                        )

                        // Apply styling based on selection
                        val isSelected = ayah.id == selectedAyahId
                        withStyle(
                            style = SpanStyle(
                                fontSize = 22.sp,
                                letterSpacing = 0.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isSelected) {
                                    androidx.compose.ui.graphics.Color(0xFF1976D2)
                                } else {
                                    androidx.compose.ui.graphics.Color.Unspecified
                                },
                                background = if (isSelected) {
                                    androidx.compose.ui.graphics.Color(0xFFE3F2FD)
                                } else {
                                    androidx.compose.ui.graphics.Color.Transparent
                                }
                            )
                        ) {
                            append(ayah.text)

                            // Add ayah number marker in a decorative format
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color(0xFF1976D2)
                                )
                            ) {
                                append(" ${convertToArabicNumerals(ayah.id)}۝ ")
                            }
                        }

                        pop()
                    }
                }
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
                    // Find which ayah was clicked
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
                                    onAyahClick(ayahId)
                                }
                            }
                        }
                }
            )

            // Show ayah actions when an ayah is selected
            if (selectedAyahId != null && selectedAyahId != -1) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                AyahActions(
                    ayahId = selectedAyahId,
                    onDismiss = { onAyahClick(-1) }
                )
            }
        }
    }
}

@Composable
private fun AyahActions(
    ayahId: Int,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ayah $ayahId",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

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

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to convert numbers to Arabic numerals
private fun convertToArabicNumerals(number: Int): String {
    val arabicNumerals = mapOf(
        '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
        '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
    )
    return number.toString().map { arabicNumerals[it] ?: it }.joinToString("")
}

@Composable
private fun SurahHeaderCard(
    surah: Surah
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

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
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
                    .height(200.dp)
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
                    text = surah.name,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Transliteration
                Text(
                    text = surah.transliteration,
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
                        label = surah.type.replaceFirstChar { it.uppercase() },
                        value = ""
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

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 24.sp,
                lineHeight = 36.sp
            )
        }
    }
}

@Composable
private fun AyahCard(
    ayah: com.example.quranapp.data.database.entities.Ayah,
    highlight: Boolean = false
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 40.sp,
                fontSize = 24.sp
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

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
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
