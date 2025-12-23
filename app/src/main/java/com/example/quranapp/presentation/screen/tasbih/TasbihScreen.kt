package com.example.quranapp.presentation.screen.tasbih

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel = hiltViewModel(),
    innerPadding: PaddingValues
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
    onSettingsClick: () -> Unit
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

@Composable
private fun DhikrDisplay(
    preset: TasbihPreset,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = preset.arabicText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preset.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = preset.translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TasbihCounter(
    count: Int,
    target: Int,
    isTargetReached: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Animation for count change
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "count"
    )

    // Scale animation on tap
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isTargetReached) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    // Progress calculation
    val progress = if (target > 0) min(count.toFloat() / target.toFloat(), 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Circular Progress Indicator
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(280.dp),
            color = if (isTargetReached) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
            strokeWidth = 12.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Main Tappable Button
        Surface(
            onClick = {
                onTap()
                // Reset pressed state after animation
                coroutineScope.launch {
                    delay(100)
                }
            },
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            shape = CircleShape,
            color = if (isTargetReached) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            tonalElevation = 8.dp,
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = animatedCount.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isTargetReached) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.5f
                )
            }
        }
    }
}

@Composable
private fun ProgressDisplay(
    count: Int,
    target: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "$count / $target",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onUndoClick: () -> Unit,
    onPresetsClick: () -> Unit,
    canUndo: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ElevatedButton(
            onClick = onUndoClick,
            enabled = canUndo,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Undo", style = MaterialTheme.typography.labelLarge)
        }

        ElevatedButton(
            onClick = onPresetsClick,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Presets", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Reset Counter?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "This will reset your current count to 0. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun PresetsDialog(
    presets: List<TasbihPreset>,
    selectedPreset: TasbihPreset,
    onPresetSelected: (TasbihPreset) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Dhikr",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    PresetItem(
                        preset = preset,
                        isSelected = preset.id == selectedPreset.id,
                        onClick = { onPresetSelected(preset) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun PresetItem(
    preset: TasbihPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.arabicText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
                Text(
                    text = "Target: ${preset.defaultTarget}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    settings: TasbihSettings,
    currentTarget: Int,
    onSettingsChanged: (TasbihSettings) -> Unit,
    onTargetChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var localSettings by remember { mutableStateOf(settings) }
    var localTarget by remember { mutableIntStateOf(currentTarget) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Haptic Feedback
                SettingsRow(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    description = "Vibrate on tap and target completion",
                    checked = localSettings.hapticFeedback,
                    onCheckedChange = {
                        localSettings = localSettings.copy(hapticFeedback = it)
                    }
                )

                HorizontalDivider()

                // Sound Feedback
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Sound Feedback",
                    description = "Play sound on tap",
                    checked = localSettings.soundFeedback,
                    onCheckedChange = {
                        localSettings = localSettings.copy(soundFeedback = it)
                    }
                )

                HorizontalDivider()

                // Auto Reset
                SettingsRow(
                    icon = Icons.Default.Autorenew,
                    title = "Auto Reset",
                    description = "Reset counter when target is reached",
                    checked = localSettings.autoReset,
                    onCheckedChange = {
                        localSettings = localSettings.copy(autoReset = it)
                    }
                )

                HorizontalDivider()

                // Custom Target
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Target",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = localTarget.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = localTarget.toFloat(),
                        onValueChange = { localTarget = it.toInt() },
                        valueRange = 1f..300f,
                        steps = 298
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSettingsChanged(localSettings)
                    onTargetChanged(localTarget)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun triggerHapticFeedback(context: Context, isTargetReached: Boolean) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
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
