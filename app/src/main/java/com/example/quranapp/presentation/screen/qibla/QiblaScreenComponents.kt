package com.example.quranapp.presentation.screen.qibla

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quranapp.R
import java.util.Locale

@Composable
fun AnimatedQiblaCard(isAligned: Boolean) {
    // Animate container color
    val containerColor by animateColorAsState(
        targetValue = if (isAligned) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "containerColor"
    )

    // Animate scale for subtle emphasis
    val scale by animateFloatAsState(
        targetValue = if (isAligned) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier
            .padding(8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        AnimatedContent(
            targetState = isAligned,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            animationSpec = tween(300),
                            initialOffsetY = { it / 4 }
                        ) togetherWith
                        fadeOut(animationSpec = tween(200)) +
                        slideOutVertically(
                            animationSpec = tween(200),
                            targetOffsetY = { -it / 4 }
                        )
            },
            label = "contentTransition"
        ) { aligned ->
            if (aligned) {
                Text(
                    text = stringResource(R.string.aligned_with_qibla),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = stringResource(R.string.rotate_to_align),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AnimatedKaabaIcon(isAligned: Boolean) {
    // Animate surface color
    val surfaceColor by animateColorAsState(
        targetValue = if (isAligned) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "surfaceColor"
    )

    // Animate icon scale with bounce when aligned
    val iconScale by animateFloatAsState(
        targetValue = if (isAligned) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    // Pulse effect when aligned
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = Modifier.size(60.dp),
        shape = CircleShape,
        color = surfaceColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            // Pulsing background circle when aligned
            if (isAligned) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(1f + pulseAlpha * 0.2f),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.3f)
                ) {}
            }

            Image(
                painter = painterResource(id = R.drawable.kaaba_selected),
                contentDescription = stringResource(R.string.kaaba),
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
        }
    }
}

@Composable
fun QiblaInfoCard(
    qiblaBearing: Float,
    distanceToKaaba: Double,
    deviceAzimuth: Float,
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    // Animate blur effect
    val blurRadius by animateDpAsState(
        targetValue = if (isAligned) 0.dp else 20.dp,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "blurRadius"
    )

    // Animate background alpha
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isAligned) 0.95f else 0.3f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "backgroundAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize(0.225f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Background image with blur effect
            val bgRes = remember { listOf(R.drawable.qaaba_1, R.drawable.qaaba_2, R.drawable.qaaba_3).random() }
            Image(
                painter = painterResource(id = bgRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = blurRadius)
                    .graphicsLayer { alpha = backgroundAlpha }
            )

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.direction_to_kaaba),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Primary info - Qibla bearing
                Text(
                    text = "${String.format(Locale.US, "%.1f", qiblaBearing)}°",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.from_north),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Secondary info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(
                            R.string.distance_format,
                            String.format(Locale.US, "%.0f", distanceToKaaba)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(
                            R.string.device_format,
                            String.format(Locale.US, "%.1f", deviceAzimuth)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}