package com.example.quranapp.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quranapp.domain.model.AsrCalculationMethod
import com.example.quranapp.domain.model.HighLatitudeMethod
import com.example.quranapp.domain.model.PrayerCalculationMethod
import kotlinx.coroutines.launch

/**
 * Prayer Time Settings Dialog
 * Allows users to configure calculation methods
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSettingsDialog(
    currentMethod: PrayerCalculationMethod,
    currentAsrMethod: AsrCalculationMethod,
    currentHighLatMethod: HighLatitudeMethod,
    onMethodSelected: (PrayerCalculationMethod) -> Unit,
    onAsrMethodSelected: (AsrCalculationMethod) -> Unit,
    onHighLatMethodSelected: (HighLatitudeMethod) -> Unit,
    onDismiss: () -> Unit,
    prayerSettingsRepository: com.example.quranapp.data.repository.PrayerSettingsRepository,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Method", "Asr", "High Lat", "Notif")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Prayer Time Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        )
                    }
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Column {
                    when (selectedTab) {
                        0 -> CalculationMethodList(
                            methods = PrayerCalculationMethod.getAllMethods(),
                            selectedMethod = currentMethod,
                            onMethodSelected = {
                                onMethodSelected(it)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "✓ Updated to ${it.displayName}",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )

                        1 -> AsrMethodList(
                            selectedMethod = currentAsrMethod,
                            onMethodSelected = {
                                onAsrMethodSelected(it)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "✓ Asr method updated",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )

                        2 -> HighLatitudeMethodList(
                            selectedMethod = currentHighLatMethod,
                            onMethodSelected = {
                                onHighLatMethodSelected(it)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "✓ High latitude method updated",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )

                        3 -> Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrayerNotificationToggle(
                                prayerSettingsRepository = prayerSettingsRepository
                            )
                        }
                    }

                    // Snackbar for feedback
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CalculationMethodList(
    methods: List<PrayerCalculationMethod>,
    selectedMethod: PrayerCalculationMethod,
    onMethodSelected: (PrayerCalculationMethod) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(methods) { method ->
            MethodItem(
                method = method,
                isSelected = method.id == selectedMethod.id,
                onClick = { onMethodSelected(method) }
            )
        }
    }
}

@Composable
private fun MethodItem(
    method: PrayerCalculationMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
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
                    text = method.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = method.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fajr: ${method.fajrAngle}°, Isha: ${method.ishaAngle?.toString() ?: "${method.ishaInterval} min"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
private fun AsrMethodList(
    selectedMethod: AsrCalculationMethod,
    onMethodSelected: (AsrCalculationMethod) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AsrCalculationMethod.entries) { method ->
            AsrMethodItem(
                method = method,
                isSelected = method == selectedMethod,
                onClick = { onMethodSelected(method) }
            )
        }
    }
}

@Composable
private fun AsrMethodItem(
    method: AsrCalculationMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
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
                    text = method.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = method.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun HighLatitudeMethodList(
    selectedMethod: HighLatitudeMethod,
    onMethodSelected: (HighLatitudeMethod) -> Unit,
) {
    Column {
        // Info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Used when sun doesn't reach required angles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HighLatitudeMethod.entries) { method ->
                HighLatMethodItem(
                    method = method,
                    isSelected = method == selectedMethod,
                    onClick = { onMethodSelected(method) }
                )
            }
        }
    }
}

@Composable
private fun HighLatMethodItem(
    method: HighLatitudeMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
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
                    text = method.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = method.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

