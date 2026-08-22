package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.data.models.ThemeMode
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.components.SettingToggleRow
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun AppearanceScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val currentTheme by viewModel.themeMode.collectAsState()
    val compactMode by viewModel.compactMode.collectAsState()
    val navBarStyle by viewModel.navigationBarStyle.collectAsState()

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Appearance",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(title = "THEME & COLOR")
            }

            item {
                SettingToggleRow(
                    title = "Dark Mode",
                    description = "Use pure AMOLED black theme",
                    icon = Icons.Outlined.Palette,
                    checked = currentTheme == ThemeMode.BLACK,
                    onCheckedChange = { isDark ->
                        coroutineScope.launch {
                            viewModel.preferences.setTheme(if (isDark) ThemeMode.BLACK else ThemeMode.WHITE)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "LAYOUT & NAVIGATION")
            }

            item {
                SettingToggleRow(
                    title = "Compact Mode",
                    description = "Reduce padding for higher information density",
                    icon = Icons.Outlined.GridView,
                    checked = compactMode,
                    onCheckedChange = {
                        coroutineScope.launch {
                            viewModel.preferences.setCompactMode(it)
                        }
                    }
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ViewStream,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Navigation Bar Style",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Default", "Chip", "Minimal").forEach { style ->
                            FilterChip(
                                selected = navBarStyle.equals(style, ignoreCase = true),
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.preferences.setNavigationBarStyle(style)
                                    }
                                },
                                label = { Text(style) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = if (colors.isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
                                    containerColor = colors.surface,
                                    labelColor = colors.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
