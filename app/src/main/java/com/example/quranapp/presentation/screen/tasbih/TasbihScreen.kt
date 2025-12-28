package com.example.quranapp.presentation.screen.tasbih

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel = hiltViewModel(),
    innerPadding: PaddingValues,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger haptic and show snackbar when target is reached
    LaunchedEffect(uiState.isTargetReached) {
        if (uiState.isTargetReached) {
            if (uiState.settings.hapticFeedback) {
                triggerHapticFeedback(context, true)
            }
            snackbarHostState.showSnackbar(
                message = "Target completed! 🎉",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TasbihTopAppBar(
                onResetClick = { viewModel.showResetDialog(true) },
                onSettingsClick = { viewModel.showSettingsDialog(true) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dhikr Display
                DhikrDisplay(
                    preset = uiState.selectedPreset,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Main Counter with Circular Progress
                TasbihCounter(
                    count = uiState.currentCount,
                    target = uiState.targetCount,
                    isTargetReached = uiState.isTargetReached,
                    onTap = {
                        viewModel.incrementCount()
                        if (uiState.settings.hapticFeedback) {
                            triggerHapticFeedback(context, false)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Display
                ProgressDisplay(
                    count = uiState.currentCount,
                    target = uiState.targetCount
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                ActionButtons(
                    onUndoClick = { viewModel.decrementCount() },
                    onPresetsClick = { viewModel.showPresetsDialog(true) },
                    canUndo = uiState.currentCount > 0
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                viewModel.resetCount()
                viewModel.showResetDialog(false)
            },
            onDismiss = { viewModel.showResetDialog(false) }
        )
    }

    if (uiState.showPresetsDialog) {
        PresetsDialog(
            presets = uiState.presets,
            selectedPreset = uiState.selectedPreset,
            onPresetSelected = {
                viewModel.selectPreset(it)
                viewModel.showPresetsDialog(false)
            },
            onDismiss = { viewModel.showPresetsDialog(false) }
        )
    }

    if (uiState.showSettingsDialog) {
        SettingsDialog(
            settings = uiState.settings,
            currentTarget = uiState.targetCount,
            onSettingsChanged = { viewModel.updateSettings(it) },
            onTargetChanged = { viewModel.updateTarget(it) },
            onDismiss = { viewModel.showSettingsDialog(false) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbihTopAppBar(
    onResetClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Tasbih",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = onResetClick) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset counter",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

private fun triggerHapticFeedback(context: Context, isTargetReached: Boolean) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    val effect = if (isTargetReached) {
        VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)
    } else {
        VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
    }
    vibrator.vibrate(effect)
}
