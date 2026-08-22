package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Margin
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.data.models.*
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.components.SettingToggleRow
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun PdfSettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val defaultPageSize by viewModel.defaultPageSize.collectAsState()
    val defaultOrientation by viewModel.defaultOrientation.collectAsState()
    val defaultMargins by viewModel.defaultMargins.collectAsState()
    val defaultQuality by viewModel.defaultImageQuality.collectAsState()
    
    val compressionLevel by viewModel.compressionLevel.collectAsState()
    val removeMetadata by viewModel.removeMetadata.collectAsState()

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "PDF Settings",
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
                SectionHeader(title = "DEFAULT PAGE SIZE")
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.Description, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Page Size", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                LazyRow(
                    modifier = Modifier.padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PageSizeOption.values().size) { idx ->
                        val opt = PageSizeOption.values()[idx]
                        FilterChip(
                            selected = defaultPageSize == opt,
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.preferences.setDefaultPageSize(opt)
                                }
                            },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = if (colors.isDark) Color.Black else Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.primary
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "DEFAULT ORIENTATION")
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.ScreenRotation, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Orientation", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                LazyRow(
                    modifier = Modifier.padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(OrientationOption.values().size) { idx ->
                        val opt = OrientationOption.values()[idx]
                        FilterChip(
                            selected = defaultOrientation == opt,
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.preferences.setDefaultOrientation(opt)
                                }
                            },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = if (colors.isDark) Color.Black else Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.primary
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "QUALITY & OPTIMIZATION")
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Compress, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Compression Level", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 34.dp)) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            FilterChip(
                                selected = compressionLevel.equals(level, ignoreCase = true),
                                onClick = {
                                    coroutineScope.launch { viewModel.preferences.setCompressionLevel(level) }
                                },
                                label = { Text(level) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = if (colors.isDark) Color.Black else Color.White,
                                    containerColor = colors.surface,
                                    labelColor = colors.primary
                                )
                            )
                        }
                    }
                }
            }

            item {
                SettingToggleRow(
                    title = "Remove Metadata",
                    description = "Strip author, date and software info from PDFs",
                    icon = Icons.Outlined.VisibilityOff,
                    checked = removeMetadata,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setRemoveMetadata(it) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "STORAGE BEHAVIOR")
                SettingToggleRow(
                    title = "Confirm Before Overwrite",
                    description = "Prompt before replacing existing PDF documents",
                    icon = Icons.Outlined.Storage,
                    checked = true, // Simplified for this task
                    onCheckedChange = {
                        coroutineScope.launch {
                            viewModel.preferences.setConfirmOverwrite(it)
                        }
                    }
                )
            }
        }
    }
}
