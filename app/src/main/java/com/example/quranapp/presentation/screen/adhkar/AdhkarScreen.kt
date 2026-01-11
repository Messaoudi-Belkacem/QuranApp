package com.example.quranapp.presentation.screen.adhkar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.quranapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    viewModel: AdhkarScreenViewModel = hiltViewModel(),
    innerPadding: PaddingValues = PaddingValues(),
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (uiState.selectedCategory != null) {
                        IconButton(onClick = { viewModel.clearSelectedCategory() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        IconButton(onClick = { onNavigateBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = uiState.selectedCategory?.titleEng ?: stringResource(R.string.adhkar),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (uiState.selectedCategory != null) {
                        IconButton(onClick = { viewModel.resetAllProgress() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.reset_all),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    PaddingValues(
                        bottom = innerPadding.calculateBottomPadding()
                    )
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }

                uiState.error != null -> {
                    ErrorState(error = uiState.error!!)
                }

                uiState.selectedCategory != null -> {
                    AdhkarDetailView(
                        adhkar = uiState.selectedCategory!!.content,
                        progress = uiState.dhikrProgress,
                        onDhikrClick = { index, maxRepeat ->
                            viewModel.incrementDhikrProgress(index, maxRepeat)
                        },
                        onResetClick = { index ->
                            viewModel.resetDhikrProgress(index)
                        }
                    )
                }

                else -> {
                    CategoriesListView(
                        categories = uiState.categories,
                        onCategoryClick = { category ->
                            viewModel.loadCategoryDetails(category.fileName)
                        }
                    )
                }
            }
        }
    }
}