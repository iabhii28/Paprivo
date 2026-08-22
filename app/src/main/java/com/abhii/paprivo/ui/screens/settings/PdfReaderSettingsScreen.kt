package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
fun PdfReaderSettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val keepAwake by viewModel.keepScreenAwake.collectAsState()
    val continuousScroll by viewModel.continuousScroll.collectAsState()
    val rememberPosition by viewModel.rememberPosition.collectAsState()
    val showPageNumber by viewModel.showPageNumbers.collectAsState()
    val defaultZoom by viewModel.defaultZoom.collectAsState()

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "PDF Reader Settings",
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
                SectionHeader(title = "READING EXPERIENCE")
            }

            item {
                SettingToggleRow(
                    title = "Keep Screen Awake",
                    description = "Prevent the screen from turning off while reading",
                    icon = Icons.Outlined.LightMode,
                    checked = keepAwake,
                    onCheckedChange = {
                        coroutineScope.launch {
                            viewModel.preferences.setKeepScreenAwake(it)
                        }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Continuous Scrolling",
                    description = "Scroll seamlessly across page boundaries",
                    icon = Icons.Outlined.SwapVert,
                    checked = continuousScroll,
                    onCheckedChange = {
                        coroutineScope.launch {
                            viewModel.preferences.setContinuousScroll(it)
                        }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Remember Reading Position",
                    description = "Resume from the last opened page",
                    icon = Icons.Outlined.Bookmark,
                    checked = rememberPosition,
                    onCheckedChange = {
                        coroutineScope.launch {
                            viewModel.preferences.setRememberPosition(it)
                        }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Show Page Number",
                    description = "Display current page overlay indicator",
                    icon = Icons.Outlined.Numbers,
                    checked = showPageNumber,
                    onCheckedChange = {
                        coroutineScope.launch {
                            viewModel.preferences.setShowPageNumbers(it)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "ZOOM SETTINGS")
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
                            text = "Default Zoom Level: ${String.format(Locale.getDefault(), "%.1f", defaultZoom)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Slider(
                        value = defaultZoom,
                        onValueChange = {
                            coroutineScope.launch {
                                viewModel.preferences.setDefaultZoom(it)
                            }
                        },
                        valueRange = 0.5f..3.0f,
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
