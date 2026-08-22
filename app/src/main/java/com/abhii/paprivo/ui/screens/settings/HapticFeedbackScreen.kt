package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.components.SettingToggleRow
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun HapticFeedbackScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val hapticIntensity by viewModel.hapticIntensity.collectAsState()

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Haptic Feedback",
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
                SectionHeader(title = "VIBRATION FEEDBACK")
            }

            item {
                SettingToggleRow(
                    title = "Haptic Feedback",
                    description = "Vibrate on button clicks, sliders, and tool actions",
                    icon = Icons.Outlined.Vibration,
                    checked = hapticsEnabled,
                    onCheckedChange = {
                        coroutineScope.launch {
                            viewModel.preferences.setHapticsEnabled(it)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "HAPTIC INTENSITY")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val intensities = listOf("soft", "medium", "strong")
                    intensities.forEach { intensity ->
                        FilterChip(
                            selected = hapticIntensity == intensity,
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.preferences.setHapticIntensity(intensity)
                                }
                            },
                            label = {
                                Text(intensity.replaceFirstChar { it.uppercase() })
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.background,
                                containerColor = colors.iconContainer,
                                labelColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            }
        }
    }
}
