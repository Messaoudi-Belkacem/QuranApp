package com.example.quranapp.presentation.screen.qibla

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.quranapp.R
import com.example.quranapp.util.RequestLocationPermission
import java.util.Locale
import kotlin.math.abs

@Composable
fun QiblaScreen(
    innerPadding: PaddingValues,
    viewModel: QiblaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val showPermissionDeniedState = remember { mutableStateOf(false) }

    RequestLocationPermission(
        onPermissionGranted = {
            viewModel.loadLocation()
        },
        onPermissionDenied = {
            showPermissionDeniedState.value = true
        }
    ) { requestPermission ->
        LaunchedEffect(Unit) {
            requestPermission()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                showPermissionDeniedState.value -> {
                    PermissionDeniedContent(
                        onRetry = {
                            showPermissionDeniedState.value = false
                            requestPermission()
                        }
                    )
                }

                !uiState.hasSensors -> {
                    NoSensorContent()
                }

                uiState.isLoadingLocation -> {
                    LoadingContent()
                }

                uiState.locationError != null -> {
                    ErrorContent(
                        error = uiState.locationError!!,
                        onRetry = { viewModel.loadLocation() }
                    )
                }

                else -> {
                    QiblaCompassContent(uiState = uiState, onRefresh = { viewModel.loadLocation() })
                }
            }
        }
    }
}

@Composable
private fun QiblaCompassContent(
    uiState: QiblaUiState,
    onRefresh: () -> Unit,
) {
    // Smooth rotation animation
    val smoothRotation by animateFloatAsState(
        targetValue = uiState.rotationAngle,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "rotation"
    )

    // Pulsing animation when aligned with Qibla
    val isAligned = abs(uiState.rotationAngle % 360) !in 5.0..355.0
    val pulseScale by animateFloatAsState(
        targetValue = if (isAligned) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top info card
        QiblaInfoCard(
            qiblaBearing = uiState.qiblaBearing,
            distanceToKaaba = uiState.distanceToKaaba,
            deviceAzimuth = uiState.deviceAzimuth,
            isAligned = isAligned,
            modifier = Modifier.fillMaxWidth()
        )

        // Kaaba icon at center
        AnimatedKaabaIcon(isAligned = isAligned)

        // Compass
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Compass background
            Image(
                painter = painterResource(id = R.drawable.compass),
                contentDescription = "Compass Background",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.8f)
                    .rotate(-uiState.deviceAzimuth),
                contentScale = ContentScale.Fit
            )

            // Azimuth arrow (points to direction device is facing)
            Image(
                painter = painterResource(id = R.drawable.compass_needle),
                contentDescription = "point Direction",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Qibla arrow (points to Kaaba)
            Image(
                painter = painterResource(id = R.drawable.kaaba_needle),
                contentDescription = "Qibla Direction",
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(smoothRotation)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                contentScale = ContentScale.Fit
            )
        }

        // Bottom info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedQiblaCard(isAligned)

            FilledTonalButton(
                onClick = onRefresh
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Location",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh Location")
            }

            Text(
                text = if (uiState.locationAddress != null) {
                    uiState.locationAddress
                } else {
                    "Lat: ${
                        String.format(
                            Locale.US,
                            "%.4f",
                            uiState.userLatitude
                        )
                    }, Lon: ${String.format(Locale.US, "%.4f", uiState.userLongitude)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            if (!uiState.isCalibrated) {
                Text(
                    text = "⚠ Calibrate your compass for better accuracy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Getting your location...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Location Error",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📍",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We need your location to calculate the direction to Kaaba.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = onRetry) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun NoSensorContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🧭",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Compass Not Available",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your device doesn't have the required sensors for compass functionality.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
