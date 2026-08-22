package com.abhii.paprivo.ui.screens.tools

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.PrimaryActionButton
import com.abhii.paprivo.ui.components.SecondaryActionButton
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MergePdfScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val selectedPdfs = remember { mutableStateListOf<Uri>() }
    var pdfName by remember { mutableStateOf("Merged_${System.currentTimeMillis() / 1000}") }
    var isProcessing by remember { mutableStateOf(false) }
    var createdFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            createdFile = null
        }
        selectedPdfs.addAll(uris)
    }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Merge PDF",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Surface(
                color = colors.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                if (createdFile != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryActionButton(
                            text = "Download",
                            onClick = { viewModel.downloadPdfFile(createdFile!!) },
                            icon = Icons.Outlined.Download,
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryActionButton(
                            text = "Share",
                            onClick = { viewModel.sharePdfFile(createdFile!!) },
                            icon = Icons.Outlined.Share,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    PrimaryActionButton(
                        text = "Merge PDFs (${selectedPdfs.size} Selected)",
                        onClick = {
                            if (selectedPdfs.size < 2) {
                                errorMessage = "Please select at least 2 PDF files to merge"
                                return@PrimaryActionButton
                            }
                            isProcessing = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = viewModel.pdfOperations.mergePdfs(selectedPdfs, pdfName)
                                isProcessing = false
                                result.onSuccess { file ->
                                    createdFile = file
                                    viewModel.loadRecentFiles()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Failed to merge PDFs"
                                }
                            }
                        },
                        enabled = selectedPdfs.size >= 2,
                        isLoading = isProcessing,
                        icon = Icons.Outlined.CallMerge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECTED PDFS (${selectedPdfs.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = colors.secondary
                    )
                    TextButton(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add PDFs", color = colors.primary, fontSize = 13.sp)
                    }
                }
            }

            if (selectedPdfs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .clickable { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Select PDF files to merge",
                                color = colors.primary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Choose 2 or more PDF documents",
                                color = colors.secondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(selectedPdfs) { index, uri ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.iconContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uri.lastPathSegment ?: "Document ${index + 1}",
                                color = colors.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        // Move Up
                        if (index > 0) {
                            IconButton(onClick = {
                                val item = selectedPdfs.removeAt(index)
                                selectedPdfs.add(index - 1, item)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = colors.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Delete
                        IconButton(onClick = { selectedPdfs.removeAt(index) }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Remove",
                                tint = colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "OUTPUT FILE NAME")
                OutlinedTextField(
                    value = pdfName,
                    onValueChange = { pdfName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primary,
                        unfocusedTextColor = colors.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
