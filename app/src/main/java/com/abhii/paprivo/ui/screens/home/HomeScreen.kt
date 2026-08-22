package com.abhii.paprivo.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.ui.components.FeatureRow
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.navigation.Screen
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val recentFiles by viewModel.recentFiles.collectAsState()

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

        // App Title - Top of screen, NO subtitle
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Paprivo",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // PDF Tools Section
        item(key = "tools_header") {
            SectionHeader(title = "PDF TOOLS")
        }

        item(key = "merge_pdf") {
            FeatureRow(
                title = "Merge PDF",
                description = "Combine multiple PDF files",
                icon = Icons.Outlined.CallMerge,
                onClick = { navController.navigate(Screen.MergePdf.route) }
            )
        }

        item(key = "split_pdf") {
            FeatureRow(
                title = "Split PDF",
                description = "Split a PDF into separate files",
                icon = Icons.Outlined.CallSplit,
                onClick = { navController.navigate(Screen.SplitPdf.route) }
            )
        }

        item(key = "compress_pdf") {
            FeatureRow(
                title = "Compress PDF",
                description = "Reduce PDF file size",
                icon = Icons.Outlined.Compress,
                onClick = { navController.navigate(Screen.CompressPdf.route) }
            )
        }

        item(key = "extract_pages") {
            FeatureRow(
                title = "Extract Pages",
                description = "Extract selected pages",
                icon = Icons.Outlined.ContentCut,
                onClick = { navController.navigate(Screen.ExtractPages.route) }
            )
        }

        item(key = "delete_pages") {
            FeatureRow(
                title = "Delete Pages",
                description = "Remove pages from a PDF",
                icon = Icons.Outlined.DeleteOutline,
                onClick = { navController.navigate(Screen.DeletePages.route) }
            )
        }

        item(key = "reorder_pages") {
            FeatureRow(
                title = "Reorder Pages",
                description = "Rearrange PDF pages",
                icon = Icons.Outlined.Reorder,
                onClick = { navController.navigate(Screen.ReorderPages.route) }
            )
        }

        item(key = "rotate_pdf") {
            FeatureRow(
                title = "Rotate PDF",
                description = "Rotate PDF pages",
                icon = Icons.Outlined.RotateRight,
                onClick = { navController.navigate(Screen.RotatePdf.route) }
            )
        }

        item(key = "pdf_to_images") {
            FeatureRow(
                title = "PDF to Images",
                description = "Convert PDF pages into images",
                icon = Icons.Outlined.Image,
                onClick = { navController.navigate(Screen.PdfToImages.route) }
            )
        }

        item(key = "pdf_viewer") {
            FeatureRow(
                title = "PDF Viewer",
                description = "Open and read PDF documents",
                icon = Icons.Outlined.Visibility,
                onClick = { navController.navigate(Screen.PdfViewer.route) }
            )
        }

        item(key = "scan_document") {
            FeatureRow(
                title = "Scan Document",
                description = "Scan documents into PDF",
                icon = Icons.Outlined.DocumentScanner,
                onClick = { navController.navigate(Screen.ScanDocument.route) }
            )
        }

        // Recent Files Section
        if (recentFiles.isNotEmpty()) {
            item(key = "recent_header") {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "RECENT FILES")
            }

            items(
                items = recentFiles.take(5),
                key = { it.id }
            ) { file ->
                FeatureRow(
                    title = file.name,
                    description = "${file.size / 1024} KB",
                    icon = Icons.Outlined.PictureAsPdf,
                    onClick = {
                        navController.navigate("${Screen.PdfViewer.route}?uri=${file.uri}")
                    }
                )
            }
        }
    }
}
