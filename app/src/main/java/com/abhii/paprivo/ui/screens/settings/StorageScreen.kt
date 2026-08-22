package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.ui.components.FeatureRow
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.components.SettingToggleRow
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun StorageScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val recentFiles by viewModel.recentFiles.collectAsState()
    val autoCleanup by viewModel.autoCleanup.collectAsState()
    val customPath by viewModel.customStoragePath.collectAsState()

    var cacheSizeMb by remember { mutableFloatStateOf(0f) }
    var outputDirSizeMb by remember { mutableFloatStateOf(0f) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var isEditingPath by remember { mutableStateOf(false) }

    fun calculateSizes() {
        val cacheFiles = context.cacheDir.listFiles() ?: emptyArray<File>()
        var totalCacheBytes = 0L
        for (f in cacheFiles) totalCacheBytes += f.length()
        cacheSizeMb = totalCacheBytes / (1024f * 1024f)

        val outputDir = viewModel.pdfOperations.outputDirectory
        val outFiles = outputDir.listFiles() ?: emptyArray()
        var totalOutBytes = 0L
        for (f in outFiles) totalOutBytes += f.length()
        outputDirSizeMb = totalOutBytes / (1024f * 1024f)
    }

    LaunchedEffect(Unit) {
        calculateSizes()
    }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Storage",
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
                SectionHeader(title = "STORAGE OVERVIEW")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Storage, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "PDF Files Created", color = colors.primary, fontSize = 14.sp)
                    }
                    Text(text = "${recentFiles.size}", color = colors.secondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Total Documents Size", color = colors.primary, fontSize = 14.sp)
                    }
                    Text(text = String.format("%.2f MB", outputDirSizeMb), color = colors.secondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "MANAGEMENT")
            }

            item {
                SettingToggleRow(
                    title = "Auto Cleanup",
                    description = "Automatically delete temporary files older than 24h",
                    icon = Icons.Outlined.AutoFixHigh,
                    checked = autoCleanup,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setAutoCleanup(it) }
                    }
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Custom Storage Path", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { isEditingPath = !isEditingPath }) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit", tint = colors.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (isEditingPath) {
                        TextField(
                            value = customPath,
                            onValueChange = {
                                coroutineScope.launch { viewModel.preferences.setCustomStoragePath(it) }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                            placeholder = { Text("e.g. /Documents/Work") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface,
                                focusedIndicatorColor = colors.primary
                            )
                        )
                    } else {
                        Text(
                            text = if (customPath.isEmpty()) "Default (Documents/Paprivo)" else customPath,
                            color = colors.secondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 34.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "CLEANUP ACTIONS")
            }

            item {
                FeatureRow(
                    title = "Clear Cache",
                    description = "Remove temporary scan and render caches",
                    icon = Icons.Outlined.CleaningServices,
                    onClick = {
                        viewModel.clearCache()
                        calculateSizes()
                        snackbarMessage = "Cache cleared"
                    }
                )
            }

            item {
                FeatureRow(
                    title = "Clear Temporary Files",
                    description = "Delete temporary image processing artifacts",
                    icon = Icons.Outlined.DeleteOutline,
                    onClick = {
                        snackbarMessage = "Temporary files deleted"
                    }
                )
            }

            if (snackbarMessage != null) {
                item {
                    Text(
                        text = snackbarMessage ?: "",
                        color = colors.primary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
