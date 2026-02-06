package com.example.quranapp.presentation.screen.adhkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quranapp.R
import com.example.quranapp.data.model.AdhkarCategoryItem
import com.example.quranapp.data.model.Dhikr
import kotlin.math.PI

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ErrorState(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun CategoriesListView(
    categories: List<AdhkarCategoryItem>,
    onCategoryClick: (AdhkarCategoryItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(categories) { _, category ->
            CategoryCard(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: AdhkarCategoryItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = category.titleEng,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AdhkarDetailView(
    adhkar: List<Dhikr>,
    progress: Map<Int, Int>,
    onDhikrClick: (Int, Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(adhkar) { index, dhikr ->
            DhikrCard(
                dhikr = dhikr,
                progress = progress[index] ?: 0,
                onTap = { onDhikrClick(index, dhikr.repeat) }
            )
        }
    }
}

@Composable
fun DhikrCard(
    dhikr: Dhikr,
    progress: Int,
    onTap: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val isCompleted = progress >= dhikr.repeat
    val progressFraction = if (dhikr.repeat > 0) progress.toFloat() / dhikr.repeat.toFloat() else 0f

    // Smooth animated progress with responsive spring animation
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "progressAnimation"
    )

    var isExpanded by remember { mutableStateOf(false) }

    // Colors for the progress border
    val progressColor = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF2196F3)
    val trackColor = Color.LightGray.copy(alpha = 0.3f)

    val cornerRadiusDp = 16.dp
    val strokeWidthDp = 4.dp

    Card(
        shape = RoundedCornerShape(cornerRadiusDp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()

                val strokeWidth = strokeWidthDp.toPx()
                val cornerRadius = cornerRadiusDp.toPx()
                val halfStroke = strokeWidth / 2

                // Calculate the total perimeter of the rounded rectangle
                val rectWidth = size.width - strokeWidth
                val rectHeight = size.height - strokeWidth
                val straightWidth = rectWidth - 2 * cornerRadius
                val straightHeight = rectHeight - 2 * cornerRadius
                val cornerArcLength = (PI.toFloat() * cornerRadius) / 2f
                val totalPerimeter = 2 * straightWidth + 2 * straightHeight + 4 * cornerArcLength

                // Draw background track (full border)
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(halfStroke, halfStroke),
                    size = Size(rectWidth, rectHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Draw progress border using PathEffect to create dash pattern
                if (animatedProgress > 0f) {
                    val progressLength = totalPerimeter * animatedProgress
                    val gapLength = totalPerimeter - progressLength

                    drawRoundRect(
                        color = progressColor,
                        topLeft = Offset(halfStroke, halfStroke),
                        size = Size(rectWidth, rectHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(progressLength, gapLength.coerceAtLeast(0.1f)),
                                phase = 0f
                            )
                        )
                    )
                }
            }
            .combinedClickable(
                onClick = {
                    if (!isCompleted) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTap()
                    }
                },
                onLongClick = { isExpanded = !isExpanded }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dhikr text
            Text(
                text = dhikr.text,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.3f
            )

            // Benefit text (expandable)
            if (dhikr.benefit.isNotEmpty()) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = dhikr.benefit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Progress counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.completed),
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "$progress / ${dhikr.repeat}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted)
                        Color(0xFF4CAF50)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}