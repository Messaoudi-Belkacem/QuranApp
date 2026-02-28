package com.example.quranapp.presentation.screen.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quranapp.R
import com.example.quranapp.presentation.navigation.Screen

@Composable
fun MoreScreen(
    innerPadding: PaddingValues,
    navController: NavHostController,
) {
    val menuGroups = remember {
        listOf(
            MenuGroup(
                titleResId = R.string.worship_tools,
                items = listOf(
                    MenuItem(
                        titleResId = R.string.prayer_times_feature,
                        descriptionResId = R.string.prayer_times_desc,
                        icon = null,
                        iconRes = R.drawable.ic_prayer_time,
                        route = Screen.PrayerTimesRoute.route
                    ),
                    MenuItem(
                        titleResId = R.string.adhkar,
                        descriptionResId = R.string.adhkar_description,
                        icon = null,
                        iconRes = R.drawable.mosque_vector,
                        route = Screen.AdkarRoute.route
                    ),
                    MenuItem(
                        titleResId = R.string.tasbih,
                        descriptionResId = R.string.tasbih_description,
                        icon = null,
                        iconRes = R.drawable.ic_tasbih,
                        route = Screen.TasbihRoute.route
                    )
                )
            ),
            MenuGroup(
                titleResId = R.string.application,
                items = listOf(
                    MenuItem(
                        titleResId = R.string.settings,
                        descriptionResId = R.string.settings_description,
                        icon = Icons.Default.Settings,
                        iconRes = null,
                        route = Screen.SettingsRoute.route
                    )
                )
            ),
            MenuGroup(
                titleResId = R.string.support_info,
                items = listOf(
                    MenuItem(
                        titleResId = R.string.help,
                        descriptionResId = R.string.help_description,
                        icon = Icons.Default.Info,
                        iconRes = null,
                        route = Screen.HelpRoute.route
                    ),
                    MenuItem(
                        titleResId = R.string.about,
                        descriptionResId = R.string.about_description,
                        icon = Icons.Default.Info,
                        iconRes = null,
                        route = Screen.AboutRoute.route
                    )
                )
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        menuGroups.forEach { group ->
            item {
                Text(
                    text = stringResource(id = group.titleResId),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            items(group.items) { menuItem ->
                MoreMenuItem(
                    item = menuItem,
                    onClick = { navController.navigate(menuItem.route) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MoreMenuItem(
    item: MenuItem,
    onClick: () -> Unit
) {
    val title = stringResource(id = item.titleResId)
    val description = item.descriptionResId?.let { stringResource(id = it) }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (item.icon != null) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            } else if (item.iconRes != null) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class MenuGroup(
    val titleResId: Int,
    val items: List<MenuItem>
)

private data class MenuItem(
    val titleResId: Int,
    val descriptionResId: Int? = null,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val route: String
)
