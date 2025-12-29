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
                title = "Worship Tools",
                items = listOf(
                    MenuItem(
                        title = "Adhkar",
                        description = "Morning & evening remembrances",
                        icon = null,
                        iconRes = R.drawable.mosque_vector,
                        route = Screen.AdkarRoute.route
                    ),
                    MenuItem(
                        title = "Tasbih",
                        description = "Digital counter for dhikr",
                        icon = null,
                        iconRes = R.drawable.ic_tasbih,
                        route = Screen.TasbihRoute.route
                    )
                )
            ),
            MenuGroup(
                title = "Application",
                items = listOf(
                    MenuItem(
                        title = "Settings",
                        description = "Language, theme, notifications",
                        icon = Icons.Default.Settings,
                        iconRes = null,
                        route = Screen.SettingsRoute.route
                    )
                )
            ),
            MenuGroup(
                title = "Support & Info",
                items = listOf(
                    MenuItem(
                        title = "Help",
                        description = "FAQs and guidance",
                        icon = Icons.Default.Info,
                        iconRes = null,
                        route = Screen.HelpRoute.route
                    ),
                    MenuItem(
                        title = "About",
                        description = "App mission and version",
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
                    text = group.title,
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
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            } else if (item.iconRes != null) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
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
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class MenuGroup(
    val title: String,
    val items: List<MenuItem>
)

private data class MenuItem(
    val title: String,
    val description: String? = null,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val route: String
)
