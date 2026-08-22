package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.util.Locale
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
fun AnimationsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val animationsEnabled by viewModel.animationsEnabled.collectAsState()
    val animationSpeed by viewModel.animationSpeed.collectAsState()

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Animations",
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
                SectionHeader(title = "MOTION & TRANSITIONS")
            }

            item {
                SettingToggleRow(
                    title = "Enable Animations",
                    description = "Smooth page transitions and list element animations",
                    icon = Icons.Outlined.Animation,
                    checked = animationsEnabled,
                    onCheckedChange = {
                        coroutineScope.launch {
                            viewModel.preferences.setAnimationsEnabled(it)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "ANIMATION SPEED")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Speed Multiplier: ${String.format(Locale.getDefault(), "%.1f", animationSpeed)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Slider(
                        value = animationSpeed,
                        onValueChange = {
                            coroutineScope.launch {
                                viewModel.preferences.setAnimationSpeed(it)
                            }
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}
