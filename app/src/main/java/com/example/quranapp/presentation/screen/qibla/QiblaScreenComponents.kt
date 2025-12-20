package com.example.quranapp.presentation.screen.qibla

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quranapp.R

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
                    text = "✓ Aligned with Qibla",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Rotate your device to align with Qibla",
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
                contentDescription = "Kaaba",
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer {
                        scaleY = iconScale
                    }
            )
        }
    }
}