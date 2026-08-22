package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.abhii.paprivo.data.models.ScanEnhancement
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.components.SettingToggleRow
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ScannerSettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val autoCrop by viewModel.preferences.scanAutoCropFlow.collectAsState(initial = true)
    val perspectiveCorrection by viewModel.preferences.scanPerspectiveFlow.collectAsState(initial = true)
    val autoEnhancement by viewModel.preferences.scanAutoEnhanceFlow.collectAsState(initial = true)
    val defaultMode by viewModel.preferences.scanDefaultModeFlow.collectAsState(initial = ScanEnhancement.COLOR)
    val autoSave by viewModel.preferences.scanAutoSaveFlow.collectAsState(initial = false)
    
    val gridLines by viewModel.gridLines.collectAsState()
    val cameraSound by viewModel.cameraSound.collectAsState()

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Scanner Settings",
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
                SectionHeader(title = "CAPTURE EXPERIENCE")
            }

            item {
                SettingToggleRow(
                    title = "Show Grid Lines",
                    description = "Display alignment grid on camera preview",
                    icon = Icons.Outlined.Grid4x4,
                    checked = gridLines,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setGridLines(it) }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Camera Sound",
                    description = "Play shutter sound when capturing documents",
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    checked = cameraSound,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setCameraSound(it) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "PROCESSING & CORRECTION")
            }

            item {
                SettingToggleRow(
                    title = "Auto Crop",
                    description = "Detect paper edges automatically",
                    icon = Icons.Outlined.CropFree,
                    checked = autoCrop,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setScanAutoCrop(it) }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Perspective Correction",
                    description = "Straighten skewed captures",
                    icon = Icons.Outlined.CenterFocusStrong,
                    checked = perspectiveCorrection,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setScanPerspective(it) }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Auto Enhancement",
                    description = "Optimize contrast and text readability automatically",
                    icon = Icons.Outlined.AutoAwesome,
                    checked = autoEnhancement,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setScanAutoEnhance(it) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "CAPTURE WORKFLOW")
                SettingToggleRow(
                    title = "Save Automatically",
                    description = "Save scanned pages immediately after capture",
                    icon = Icons.Outlined.Save,
                    checked = autoSave,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setScanAutoSave(it) }
                    }
                )
            }
        }
    }
}
