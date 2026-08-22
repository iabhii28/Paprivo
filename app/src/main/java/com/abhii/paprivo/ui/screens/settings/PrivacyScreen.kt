package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun PrivacyScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val showRecentHistory by viewModel.preferences.privacyRecentFilesFlow.collectAsState(initial = true)
    val saveSearchHistory by viewModel.preferences.privacySearchHistoryFlow.collectAsState(initial = true)
    val generateThumbnails by viewModel.preferences.privacyThumbnailsFlow.collectAsState(initial = true)
    
    val autoClearCache by viewModel.autoClearCache.collectAsState()
    val incognitoMode by viewModel.incognitoMode.collectAsState()

    var actionMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Privacy",
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
                SectionHeader(title = "BROWSING")
            }

            item {
                SettingToggleRow(
                    title = "Incognito Mode",
                    description = "Stop saving history and thumbnails temporarily",
                    icon = Icons.Outlined.VisibilityOff,
                    checked = incognitoMode,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setIncognitoMode(it) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "LOCAL DATA SETTINGS")
            }

            item {
                SettingToggleRow(
                    title = "Recent Files History",
                    description = "Maintain a local list of recently created PDF documents",
                    icon = Icons.Outlined.History,
                    checked = showRecentHistory,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setPrivacyRecentFiles(it) }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Search History",
                    description = "Remember search queries locally on device",
                    icon = Icons.Outlined.Search,
                    checked = saveSearchHistory,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setPrivacySearchHistory(it) }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Generate Thumbnails",
                    description = "Cache local image previews of PDF first pages",
                    icon = Icons.Outlined.Image,
                    checked = generateThumbnails,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setPrivacyThumbnails(it) }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Auto Clear Cache",
                    description = "Clear temporary files when app is closed",
                    icon = Icons.Outlined.CleaningServices,
                    checked = autoClearCache,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setAutoClearCache(it) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "PRIVACY ACTIONS")
            }

            item {
                FeatureRow(
                    title = "Clear Recent History",
                    description = "Reset recent documents list",
                    icon = Icons.Outlined.History,
                    onClick = {
                        viewModel.clearRecentHistory()
                        actionMessage = "Recent documents history cleared"
                    }
                )
            }

            item {
                FeatureRow(
                    title = "Clear Search History",
                    description = "Erase all saved search strings",
                    icon = Icons.Outlined.CleaningServices,
                    onClick = {
                        actionMessage = "Search history cleared"
                    }
                )
            }

            item {
                FeatureRow(
                    title = "Clear All Local Data",
                    description = "Remove all cached files and preferences",
                    icon = Icons.Outlined.DeleteForever,
                    onClick = {
                        viewModel.clearAllData()
                        actionMessage = "All local data reset successfully"
                    }
                )
            }

            if (actionMessage != null) {
                item {
                    Text(
                        text = actionMessage ?: "",
                        color = colors.primary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
