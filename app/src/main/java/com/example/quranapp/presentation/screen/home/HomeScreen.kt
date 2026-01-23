package com.example.quranapp.presentation.screen.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbShade
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quranapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home Screen - Main dashboard showing current time, prayer times, and quick navigation
 */
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToQuran: () -> Unit = {},
    onNavigateToAdhkar: () -> Unit = {},
    onNavigateToTasbih: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refreshLocationAndPrayerTimes() },
        state = pullToRefreshState,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            CurrentTimeDisplay(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )

            if (uiState.nextPrayer != null || uiState.lastPrayer != null) {
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
                onSettings = { viewModel.showPrayerSettings(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            NavigationButtonsRow(
                onNavigateToQuran = onNavigateToQuran,
                onNavigateToAdhkar = onNavigateToAdhkar,
                onNavigateToTasbih = onNavigateToTasbih
            )
        }
    }

    if (uiState.showPrayerSettings) {
        PrayerSettingsDialog(
            currentMethod = uiState.calculationMethod,
            currentAsrMethod = uiState.asrMethod,
            currentHighLatMethod = uiState.highLatMethod,
            onMethodSelected = { viewModel.updateCalculationMethod(it) },
            onAsrMethodSelected = { viewModel.updateAsrMethod(it) },
            onHighLatMethodSelected = { viewModel.updateHighLatMethod(it) },
            onDismiss = { viewModel.showPrayerSettings(false) },
            prayerSettingsRepository = viewModel.getPrayerSettingsRepository()
        )
    }
}

/**
 * Displays prayer time progress between last and next prayer
 * Shows countdown to next prayer and time elapsed since last prayer
 */
@Composable
fun PrayerTimeProgress(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
) {
    var now by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            now = Date()
            delay(1000L)
        }
    }

    val progress = remember(now, uiState.nextPrayer, uiState.lastPrayer) {
        if (uiState.nextPrayer?.dateTime != null && uiState.lastPrayer?.dateTime != null) {
            val totalTime = uiState.nextPrayer.dateTime.time - uiState.lastPrayer.dateTime.time
            val elapsed = now.time - uiState.lastPrayer.dateTime.time
            (elapsed.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.nextPrayer?.dateTime != null && uiState.lastPrayer?.dateTime != null) {
                ProgressBar(
                    progress = progress,
                    lastPrayerName = uiState.lastPrayer.name,
                    nextPrayerName = uiState.nextPrayer.name
                )
            }

            if (uiState.nextPrayer?.dateTime != null) {
                NextPrayerCard(
                    prayerData = uiState.nextPrayer,
                    now = now
                )
            }

            if (uiState.lastPrayer?.dateTime != null) {
                LastPrayerCard(
                    prayerData = uiState.lastPrayer,
                    now = now
                )
            }
        }
    }
}

/**
 * Progress bar between last and next prayer
 */
@Composable
private fun ProgressBar(
    progress: Float,
    lastPrayerName: String,
    nextPrayerName: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = getLocalizedPrayerName(lastPrayerName),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = getLocalizedPrayerName(nextPrayerName),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Card showing next prayer details
 */
@Composable
private fun NextPrayerCard(prayerData: PrayerTimeData, now: Date) {
    val timeDiff = prayerData.dateTime!!.time - now.time
    val timeRemaining = formatTimeDifference(timeDiff)

    PrayerCard(
        titleRes = R.string.next_prayer,
        prayerName = getLocalizedPrayerName(prayerData.name),
        prayerTime = prayerData.time,
        timeDifference = timeRemaining,
        differenceLabel = stringResource(R.string.remaining),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
        accentColor = MaterialTheme.colorScheme.primary
    )
}

/**
 * Card showing last prayer details
 */
@Composable
private fun LastPrayerCard(prayerData: PrayerTimeData, now: Date) {
    val timeDiff = now.time - prayerData.dateTime!!.time
    val timePassed = formatTimeDifference(timeDiff)

    PrayerCard(
        titleRes = R.string.last_prayer,
        prayerName = getLocalizedPrayerName(prayerData.name),
        prayerTime = prayerData.time,
        timeDifference = timePassed,
        differenceLabel = stringResource(R.string.passed),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
        accentColor = MaterialTheme.colorScheme.secondary
    )
}

/**
 * Reusable prayer information card
 */
@Composable
private fun PrayerCard(
    titleRes: Int,
    prayerName: String,
    prayerTime: String,
    timeDifference: String,
    differenceLabel: String,
    containerColor: androidx.compose.ui.graphics.Color,
    onContainerColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = titleRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = prayerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onContainerColor
            )
            Text(
                text = "${stringResource(R.string.at)} $prayerTime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = timeDifference,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = differenceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Formats millisecond difference into human-readable time format
 * @param millisDiff Time difference in milliseconds
 * @return Formatted string like "2h 30m" or "45m 12s"
 */
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

/**
 * Returns localized prayer name from string resources
 */
@Composable
private fun getLocalizedPrayerName(prayerName: String): String {
    return when (prayerName) {
        "Fajr" -> stringResource(R.string.fajr)
        "Sunrise" -> stringResource(R.string.sunrise)
        "Dhuhr" -> stringResource(R.string.dhuhr)
        "Asr" -> stringResource(R.string.asr)
        "Maghrib" -> stringResource(R.string.maghrib)
        "Isha" -> stringResource(R.string.isha)
        else -> prayerName
    }
}

/**
 * Displays current time with date in a prominent format
 */
@Composable
fun CurrentTimeDisplay(
    modifier: Modifier = Modifier,
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm", Locale.getDefault()) }
    val amPmFormatter = remember { SimpleDateFormat("a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()) }

    var now by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            now = Date()
            delay(1000L)
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Time display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeFormatter.format(now),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = amPmFormatter.format(now),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = dateFormatter.format(now),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Component showing prayer times with location, settings, and refresh options
 */
@Composable
fun PrayerTimesComponent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onRefresh: () -> Unit = {},
    onSettings: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PrayerTimesHeader(
            currentLocation = uiState.currentLocation,
            locationAddress = uiState.locationAddress,
            onSettings = onSettings
        )

        when {
            uiState.isLoading -> LoadingState()
            uiState.error != null -> ErrorState(error = uiState.error, onRefresh = onRefresh)
            else -> PrayerTimesList(prayerTimes = uiState.prayerTimes)
        }
    }
}

/**
 * Header with title, settings button, and location indicator
 */
@Composable
private fun PrayerTimesHeader(
    currentLocation: com.example.quranapp.data.model.Location?,
    locationAddress: String?,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.prayer_times),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.prayer_settings),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (currentLocation != null || locationAddress != null) {
                LocationIndicator(locationAddress = locationAddress, currentLocation = currentLocation)
            }
        }
    }
}

/**
 * Shows current location as a chip
 */
@Composable
private fun LocationIndicator(
    locationAddress: String?,
    currentLocation: com.example.quranapp.data.model.Location?
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = stringResource(R.string.location),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = locationAddress ?: currentLocation.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Loading state with spinner and message
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp
            )
            Text(
                text = stringResource(R.string.loading_prayer_times),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state with message and retry button
 */
@Composable
private fun ErrorState(error: String, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = stringResource(R.string.error),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onRefresh,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * Horizontal scrollable list of prayer times
 */
@Composable
private fun PrayerTimesList(prayerTimes: List<PrayerTimeData>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(prayerTimes) { prayerTime ->
            PrayerTimeItem(prayerTimeData = prayerTime)
        }
    }
}

/**
 * Individual prayer time card showing icon, name, and time
 */
@Composable
fun PrayerTimeItem(prayerTimeData: PrayerTimeData) {
    val localizedName = getLocalizedPrayerName(prayerTimeData.name)
    val icon = getPrayerIcon(prayerTimeData.name)
    val contentDesc = getPrayerContentDescription(prayerTimeData.name)

    Card(
        modifier = Modifier.width(130.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = localizedName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = prayerTimeData.time,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Returns appropriate icon for each prayer
 */
private fun getPrayerIcon(prayerName: String): ImageVector {
    return when (prayerName) {
        "Fajr" -> Icons.Default.WbTwilight
        "Sunrise" -> Icons.Default.WbSunny
        "Dhuhr" -> Icons.Default.WbSunny
        "Asr" -> Icons.Default.WbCloudy
        "Maghrib" -> Icons.Default.WbShade
        "Isha" -> Icons.Default.NightsStay
        else -> Icons.Default.WbSunny
    }
}

/**
 * Returns localized content description for each prayer
 */
@Composable
private fun getPrayerContentDescription(prayerName: String): String {
    return when (prayerName) {
        "Fajr" -> stringResource(R.string.fajr_prayer)
        "Sunrise" -> stringResource(R.string.sunrise_prayer)
        "Dhuhr" -> stringResource(R.string.dhuhr_prayer)
        "Asr" -> stringResource(R.string.asr_prayer)
        "Maghrib" -> stringResource(R.string.maghrib_prayer)
        "Isha" -> stringResource(R.string.isha_prayer)
        else -> prayerName
    }
}

/**
 * Quick navigation buttons to main app features
 */
@Composable
fun NavigationButtonsRow(
    onNavigateToQuran: () -> Unit = {},
    onNavigateToAdhkar: () -> Unit = {},
    onNavigateToTasbih: () -> Unit = {},
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            NavigationButton(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = stringResource(R.string.quran),
                onClick = onNavigateToQuran
            )
        }
        item {
            NavigationButton(
                icon = Icons.AutoMirrored.Filled.List,
                label = stringResource(R.string.adhkar),
                onClick = onNavigateToAdhkar
            )
        }
        item {
            NavigationButton(
                icon = Icons.Default.SportsHandball,
                label = stringResource(R.string.tasbih),
                onClick = onNavigateToTasbih
            )
        }
    }
}

/**
 * Individual navigation button with icon and label
 */
@Composable
private fun NavigationButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}