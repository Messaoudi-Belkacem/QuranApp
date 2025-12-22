package com.example.quranapp.presentation.screen.surah

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(surahId) {
        Log.d("SurahReading", "Requesting loadSurah($surahId)")
        viewModel.loadSurah(surahId)
    }

    LaunchedEffect(uiState) {
        Log.d("SurahReading", "uiState changed: isLoading=${uiState.isLoading}, ayahs=${uiState.ayahs.size}, error=${uiState.errorMessage}")
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                showScrollToTop = index > 5
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // Top App Bar
            SurahTopAppBar(
                surah = uiState.currentSurah,
                onBackClick = onBackClick,
                isLoading = uiState.isLoading
            )

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

                uiState.currentSurah != null && uiState.ayahs.isNotEmpty() -> {
                    SurahContent(
                        surah = uiState.currentSurah!!,
                        ayahs = uiState.ayahs,
                        listState = listState
                    )
                }

                else -> {
                    EmptyContent()
                }
            }
        }

        // Floating Action Button for scroll to top
        AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to top"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahTopAppBar(
    surah: com.example.quranapp.data.database.entities.Surah?,
    onBackClick: () -> Unit,
    isLoading: Boolean
) {
    TopAppBar(
        title = {
            if (surah != null) {
                Column {
                    Text(
                        text = surah.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = surah.transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isLoading) {
                Text("Loading...")
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
            IconButton(onClick = { /* Handle bookmark */ }) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark"
                )
            }
            IconButton(onClick = { /* Handle settings */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun SurahContent(
    surah: com.example.quranapp.data.database.entities.Surah,
    ayahs: List<com.example.quranapp.data.database.entities.Ayah>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Surah Header
        item {
            SurahHeaderCard(surah = surah)
        }

        // Bismillah (except for Surah At-Tawbah)
        if (surah.id != 9) {
            item {
                BismillahCard()
            }
        }

        // Ayahs
        items(
            items = ayahs,
            key = { ayah -> ayah.id }
        ) { ayah ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                AyahCard(ayah = ayah)
            }
        }

        // Bottom spacer
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SurahHeaderCard(
    surah: com.example.quranapp.data.database.entities.Surah
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surah.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = surah.transliteration,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(
                    label = "Ayahs",
                    value = surah.totalVerses.toString()
                )

                InfoChip(
                    label = "Type",
                    value = surah.type
                )

                InfoChip(
                    label = "Order",
                    value = surah.id.toString()
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BismillahCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun AyahCard(
    ayah: com.example.quranapp.data.database.entities.Ayah
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Ayah number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                ) {
                    Text(
                        text = ayah.id.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Row {
                    IconButton(
                        onClick = { /* Handle bookmark */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Ayah",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { /* Handle copy */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Ayah",
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 40.sp,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Translation
            Text(
                text = ayah.text,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Additional info
            /*Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (ayah.juzNumber > 0) {
                    Text(
                        text = "Juz ${ayah.juzNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (ayah.hizbNumber > 0) {
                    Text(
                        text = "Hizb ${ayah.hizbNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }*/
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading Surah...",
                style = MaterialTheme.typography.bodyLarge,
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
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Failed to Load Surah",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
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
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = "No content",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Ayahs Found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This Surah doesn't have any Ayahs available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
