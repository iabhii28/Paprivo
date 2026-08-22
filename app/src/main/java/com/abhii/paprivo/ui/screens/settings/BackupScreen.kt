package com.abhii.paprivo.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.EnhancedEncryption
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun BackupScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val autoBackup by viewModel.autoBackupEnabled.collectAsState()
    val encryption by viewModel.backupEncryption.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importSettings(uri)
        }
    }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Backup & Restore",
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
                SectionHeader(title = "AUTOMATION")
            }

            item {
                SettingToggleRow(
                    title = "Auto Backup",
                    description = "Back up settings to cloud every 7 days",
                    icon = Icons.Outlined.Backup,
                    checked = autoBackup,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setAutoBackupEnabled(it) }
                    }
                )
            }

            item {
                SettingToggleRow(
                    title = "Backup Encryption",
                    description = "Encrypt exported JSON with device key",
                    icon = Icons.Outlined.EnhancedEncryption,
                    checked = encryption,
                    onCheckedChange = {
                        coroutineScope.launch { viewModel.preferences.setBackupEncryption(it) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "EXPORT")
            }

            item {
                FeatureRow(
                    title = "Export Settings",
                    description = "Export your app settings and preferences as JSON",
                    icon = Icons.Outlined.CloudUpload,
                    onClick = {
                        viewModel.exportSettings()
                    }
                )
            }

            item {
                FeatureRow(
                    title = "Export Favorites",
                    description = "Backup your favorited quick tools and layouts",
                    icon = Icons.Outlined.StarBorder,
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Paprivo_Favorites.json")
                            putExtra(Intent.EXTRA_TEXT, "{\"favorites\":[\"IMAGE_TO_PDF\",\"MERGE_PDF\"]}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export Favorites"))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "RESTORE")
            }

            item {
                FeatureRow(
                    title = "Import Backup",
                    description = "Restore settings from a previously exported JSON backup",
                    icon = Icons.Outlined.CloudDownload,
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/plain"))
                    }
                )
            }

            if (statusMessage != null) {
                item {
                    Text(
                        text = statusMessage ?: "",
                        color = colors.primary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
