package com.abhii.paprivo.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
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
import com.abhii.paprivo.config.AppConfig
import com.abhii.paprivo.ui.components.FeatureRow
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel

@Composable
fun AboutScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val colors = LocalPaprivoColors.current

    val updateInfo by viewModel.updateInfo.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()

    var showChangelogDialog by remember { mutableStateOf(false) }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // handle
        }
    }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "About",
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.iconContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FormatListBulleted,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = AppConfig.APP_NAME,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version ${AppConfig.APP_VERSION}",
                        fontSize = 14.sp,
                        color = colors.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Developed By AbhiI & Swastik",
                        fontSize = 13.sp,
                        color = colors.secondary
                    )
                }
            }

            item {
                SectionHeader(title = "UPDATES & CHANGELOG")
            }

            item {
                FeatureRow(
                    title = "Check for Updates",
                    description = if (isCheckingUpdate) "Checking remote servers..." else "Check for latest releases",
                    icon = Icons.Outlined.SystemUpdate,
                    onClick = {
                        viewModel.checkForUpdates(showNoUpdateToast = true)
                    }
                )
            }

            item {
                FeatureRow(
                    title = "Changelog",
                    description = "What's new in V1.0.0",
                    icon = Icons.Outlined.History,
                    onClick = {
                        showChangelogDialog = true
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "LINKS & COMMUNITY")
            }

            item {
                FeatureRow(
                    title = "GitHub Repository",
                    description = "Source code, issues, and contributions",
                    icon = Icons.Outlined.Code,
                    onClick = {
                        openUrl(AppConfig.GITHUB_URL)
                    }
                )
            }

            item {
                FeatureRow(
                    title = "Telegram Channel",
                    description = "Official announcements and discussion",
                    icon = Icons.AutoMirrored.Outlined.Send,
                    onClick = {
                        openUrl(AppConfig.TELEGRAM_URL)
                    }
                )
            }
        }
    }

    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            confirmButton = {
                TextButton(onClick = { showChangelogDialog = false }) {
                    Text("Close", color = colors.primary)
                }
            },
            title = {
                Text(
                    text = "Paprivo V1.0.0",
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Native Kotlin & Jetpack Compose utility architecture", color = colors.primary, fontSize = 13.sp)
                    Text("• Insta Archive style minimalist typography and list layout", color = colors.primary, fontSize = 13.sp)
                    Text("• AMOLED Black (#000000) and Clean White (#FFFFFF) themes", color = colors.primary, fontSize = 13.sp)
                    Text("• 100% Offline PDF Tools (Image to PDF, Merge, Split, Compress, Extract, Rotate, Delete, Reorder)", color = colors.primary, fontSize = 13.sp)
                    Text("• Integrated document scanner with perspective correction and enhancement filters", color = colors.primary, fontSize = 13.sp)
                    Text("• Full-featured on-device PDF viewer with pinch-to-zoom", color = colors.primary, fontSize = 13.sp)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
