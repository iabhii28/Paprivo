package com.abhii.paprivo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.ui.components.FeatureRow
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.navigation.Screen
import com.abhii.paprivo.ui.theme.LocalPaprivoColors

@Composable
fun SettingsScreen(
    navController: NavController
) {
    val colors = LocalPaprivoColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top spacing to allow background to bleed into status bar
        item {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // APPEARANCE
        item {
            SectionHeader(title = "APPEARANCE")
        }

        item {
            FeatureRow(
                title = "Appearance",
                description = "Theme and interface customization",
                icon = Icons.Outlined.Palette,
                onClick = { navController.navigate(Screen.SettingsAppearance.route) }
            )
        }

        item {
            FeatureRow(
                title = "Animations",
                description = "Control animations and transitions",
                icon = Icons.Outlined.Animation,
                onClick = { navController.navigate(Screen.SettingsAnimations.route) }
            )
        }

        item {
            FeatureRow(
                title = "Haptic Feedback",
                description = "Control vibration feedback",
                icon = Icons.Outlined.Vibration,
                onClick = { navController.navigate(Screen.SettingsHaptics.route) }
            )
        }

        // PDF
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "PDF")
        }

        item {
            FeatureRow(
                title = "PDF Settings",
                description = "Default PDF creation preferences",
                icon = Icons.Outlined.PictureAsPdf,
                onClick = { navController.navigate(Screen.SettingsPdf.route) }
            )
        }

        item {
            FeatureRow(
                title = "PDF Reader",
                description = "Reading preferences",
                icon = Icons.Outlined.MenuBook,
                onClick = { navController.navigate(Screen.SettingsPdfReader.route) }
            )
        }

        // SCANNER
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "SCANNER")
        }

        item {
            FeatureRow(
                title = "Scanner",
                description = "Scanning preferences",
                icon = Icons.Outlined.DocumentScanner,
                onClick = { navController.navigate(Screen.SettingsScanner.route) }
            )
        }

        // STORAGE
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "STORAGE")
        }

        item {
            FeatureRow(
                title = "Storage",
                description = "Manage local files and cache",
                icon = Icons.Outlined.Folder,
                onClick = { navController.navigate(Screen.SettingsStorage.route) }
            )
        }

        // PRIVACY
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "PRIVACY")
        }

        item {
            FeatureRow(
                title = "Privacy",
                description = "Privacy and local data controls",
                icon = Icons.Outlined.Shield,
                onClick = { navController.navigate(Screen.SettingsPrivacy.route) }
            )
        }

        // BACKUP
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "BACKUP")
        }

        item {
            FeatureRow(
                title = "Backup",
                description = "Export and restore application settings",
                icon = Icons.Outlined.CloudSync,
                onClick = { navController.navigate(Screen.SettingsBackup.route) }
            )
        }

        // ABOUT
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "ABOUT")
        }

        item {
            FeatureRow(
                title = "About",
                description = "App information and developer",
                icon = Icons.Outlined.Info,
                onClick = { navController.navigate(Screen.SettingsAbout.route) }
            )
        }
    }
}
