package com.example.quranapp.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbShade
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    var pullOffset by remember { mutableFloatStateOf(0f) }
    val refreshThreshold = 150f

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    // Animated rotation for refresh indicator
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Scale animation for pull gesture
    val pullScale by animateFloatAsState(
        targetValue = if (pullOffset > 0) 1f + (pullOffset / 1000f) else 1f,
        animationSpec = tween(100),
        label = "pullScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f * (1 - gradientOffset)),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f * gradientOffset)
                    )
                )
            )
            .padding(innerPadding)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (pullOffset >= refreshThreshold && !refreshing) {
                            refreshing = true
                            scope.launch {
                                viewModel.refreshPrayerTimes()
                                delay(500)
                                refreshing = false
                            }
                        }
                        pullOffset = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0 && !refreshing) {
                            pullOffset = (pullOffset + dragAmount).coerceIn(0f, refreshThreshold * 1.5f)
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, pullOffset.roundToInt()) }
                .scale(pullScale)
        ) {
            CurrentTimeDisplay(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            AnimatedVisibility(
                visible = uiState.nextPrayer != null || uiState.lastPrayer != null,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(600)),
                exit = slideOutVertically() + fadeOut()
            ) {
                PrayerTimeProgress(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            PrayerTimesComponent(
                uiState = uiState,
                onRefresh = { viewModel.refreshPrayerTimes() },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        // Animated refresh indicator
        AnimatedVisibility(
            visible = refreshing || pullOffset > 0,
            enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, (pullOffset * 0.5f).roundToInt()) }
                .padding(top = 16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .rotate(if (refreshing) rotation else 0f),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PrayerTimeProgress(
    modifier: Modifier = Modifier,
    uiState: HomeUiState
) {
    var now by remember { mutableStateOf(Date()) }

    // Update current time every second for live countdown
    LaunchedEffect(Unit) {
        while (isActive) {
            now = Date()
            delay(1000L)
        }
    }

    // Calculate progress percentage for visual indicator
    val progress = remember(now, uiState.nextPrayer, uiState.lastPrayer) {
        if (uiState.nextPrayer?.dateTime != null && uiState.lastPrayer?.dateTime != null) {
            val totalTime = uiState.nextPrayer.dateTime.time - uiState.lastPrayer.dateTime.time
            val elapsed = now.time - uiState.lastPrayer.dateTime.time
            (elapsed.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Pulsing animation for urgency (when prayer is close)
    val urgentPulse = remember(uiState.nextPrayer, now) {
        if (uiState.nextPrayer?.dateTime != null) {
            val remaining = uiState.nextPrayer.dateTime.time - now.time
            remaining < 5 * 60 * 1000 // Less than 5 minutes
        } else false
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress bar
            if (uiState.nextPrayer?.dateTime != null && uiState.lastPrayer?.dateTime != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (urgentPulse)
                            MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha)
                        else
                            MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.lastPrayer.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.nextPrayer.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Next Prayer Section
            AnimatedVisibility(
                visible = uiState.nextPrayer?.dateTime != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                if (uiState.nextPrayer?.dateTime != null) {
                    val timeDiff = uiState.nextPrayer.dateTime.time - now.time
                    val timeRemaining = formatTimeDifference(timeDiff)

                    val backgroundColor by animateColorAsState(
                        targetValue = if (urgentPulse)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = pulseAlpha * 0.5f)
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        animationSpec = tween(500),
                        label = "nextPrayerBg"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Next Prayer",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.nextPrayer.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (urgentPulse)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "at ${uiState.nextPrayer.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.alpha(if (urgentPulse) pulseAlpha else 1f)
                            ) {
                                Text(
                                    text = timeRemaining,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (urgentPulse)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Last Prayer Section
            AnimatedVisibility(
                visible = uiState.lastPrayer?.dateTime != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                if (uiState.lastPrayer?.dateTime != null) {
                    val timeDiff = now.time - uiState.lastPrayer.dateTime.time
                    val timePassed = formatTimeDifference(timeDiff)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Last Prayer",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.lastPrayer.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "at ${uiState.lastPrayer.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = timePassed,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "passed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimeDifference(millisDiff: Long): String {
    val totalSeconds = millisDiff / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
        minutes > 0 -> String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds)
        else -> String.format(Locale.getDefault(), "%ds", seconds)
    }
}

@Composable
fun CurrentTimeDisplay(
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm", Locale.getDefault()) }
    val amPmFormatter = remember { SimpleDateFormat("a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()) }

    var now by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            now = Date()
            delay(1000L) // Update every second for smooth colon animation
        }
    }

    // Pulsing animation for time separator
    val pulseTransition = rememberInfiniteTransition(label = "timePulse")
    val colonAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colonAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Time display with animated colon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val timeParts = timeFormatter.format(now).split(":")
                Text(
                    text = timeParts[0],
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.alpha(colonAlpha),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = timeParts[1],
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = amPmFormatter.format(now),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = dateFormatter.format(now),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PrayerTimesComponent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onRefresh: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prayer Times",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            // Animated location indicator if available
            if (uiState.currentLocation != null) {
                val pulseTransition = rememberInfiniteTransition(label = "locationPulse")
                val pulseScale by pulseTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Loading prayer times...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.error != null -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    CircleShape
                                )
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbCloudy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val buttonScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(300),
                            label = "buttonScale"
                        )

                        androidx.compose.material3.Button(
                            onClick = onRefresh,
                            modifier = Modifier.scale(buttonScale),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(uiState.prayerTimes) { index, prayerTime ->
                        var visible by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            delay(index * 100L) // Staggered animation
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            ) + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            PrayerTimeItemFromViewModel(
                                prayerTimeData = prayerTime
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerTimeItemFromViewModel(
    prayerTimeData: PrayerTimeData
) {
    // Map prayer names to icons
    val icon = when (prayerTimeData.name) {
        "Fajr" -> Icons.Default.WbTwilight
        "Sunrise" -> Icons.Default.WbSunny
        "Dhuhr" -> Icons.Default.WbSunny
        "Asr" -> Icons.Default.WbCloudy
        "Maghrib" -> Icons.Default.WbShade
        "Isha" -> Icons.Default.NightsStay
        else -> Icons.Default.WbSunny
    }

    // Vibrant colors for each prayer
    val iconColor = when (prayerTimeData.name) {
        "Fajr" -> Color(0xFF6A4C93)  // Purple
        "Sunrise" -> Color(0xFFFFA500) // Orange
        "Dhuhr" -> Color(0xFFFFD700)  // Gold
        "Asr" -> Color(0xFF20B2AA)    // Light Sea Green
        "Maghrib" -> Color(0xFFFF6B6B) // Red
        "Isha" -> Color(0xFF4A5568)   // Dark Gray
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                iconColor.copy(alpha = 0.2f),
                                iconColor.copy(alpha = 0.05f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "${prayerTimeData.name} prayer",
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = prayerTimeData.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = prayerTimeData.time,
                style = MaterialTheme.typography.bodyLarge,
                color = iconColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}