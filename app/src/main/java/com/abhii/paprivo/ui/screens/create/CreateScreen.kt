package com.abhii.paprivo.ui.screens.create

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
fun CreateScreen(
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

        // Title - No subtitle
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Create",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // FROM IMAGES
        item {
            SectionHeader(title = "FROM IMAGES")
        }

        item {
            FeatureRow(
                title = "JPG to PDF",
                description = "Convert JPG image files to PDF",
                icon = Icons.Outlined.Image,
                onClick = { navController.navigate("${Screen.ImageToPdf.route}?type=jpg") }
            )
        }

        item {
            FeatureRow(
                title = "JPEG to PDF",
                description = "Convert JPEG image files to PDF",
                icon = Icons.Outlined.Image,
                onClick = { navController.navigate("${Screen.ImageToPdf.route}?type=jpeg") }
            )
        }

        item {
            FeatureRow(
                title = "PNG to PDF",
                description = "Convert PNG images with transparency",
                icon = Icons.Outlined.Image,
                onClick = { navController.navigate("${Screen.ImageToPdf.route}?type=png") }
            )
        }

        item {
            FeatureRow(
                title = "WEBP to PDF",
                description = "Convert high compression WEBP images",
                icon = Icons.Outlined.Image,
                onClick = { navController.navigate("${Screen.ImageToPdf.route}?type=webp") }
            )
        }

        item {
            FeatureRow(
                title = "HEIC to PDF",
                description = "Convert Apple HEIC photos to PDF",
                icon = Icons.Outlined.Image,
                onClick = { navController.navigate("${Screen.ImageToPdf.route}?type=heic") }
            )
        }

        item {
            FeatureRow(
                title = "HEIF to PDF",
                description = "Convert HEIF high efficiency images",
                icon = Icons.Outlined.Image,
                onClick = { navController.navigate("${Screen.ImageToPdf.route}?type=heif") }
            )
        }

        item {
            FeatureRow(
                title = "Images to PDF",
                description = "Batch convert multiple mixed images",
                icon = Icons.Outlined.Collections,
                onClick = { navController.navigate(Screen.ImageToPdf.route) }
            )
        }

        // FROM TEXT
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "FROM TEXT")
        }

        item {
            FeatureRow(
                title = "Text to PDF",
                description = "Create PDF documents from plain text or notes",
                icon = Icons.Outlined.TextFields,
                onClick = { navController.navigate(Screen.TextToPdf.route) }
            )
        }

        // OTHER
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "OTHER")
        }

        item {
            FeatureRow(
                title = "Blank PDF",
                description = "Create blank PDF canvas pages",
                icon = Icons.Outlined.NoteAdd,
                onClick = { navController.navigate(Screen.BlankPdf.route) }
            )
        }

        // SCAN
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "SCAN")
        }

        item {
            FeatureRow(
                title = "Scan Document",
                description = "Capture and convert paper documents",
                icon = Icons.Outlined.DocumentScanner,
                onClick = { navController.navigate(Screen.ScanDocument.route) }
            )
        }
    }
}
