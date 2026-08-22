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
fun ReorderPagesScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    val pageOrder = remember { mutableStateListOf<Int>() }
    var pdfName by remember { mutableStateOf("Reordered_${System.currentTimeMillis() / 1000}") }
    var isProcessing by remember { mutableStateOf(false) }
    var createdFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            coroutineScope.launch {
                val count = viewModel.pdfOperations.getPdfPageCount(uri)
                pageOrder.clear()
                for (i in 0 until count) pageOrder.add(i)
            }
        }
    }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Reorder Pages",
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
                        text = "Save Page Order",
                        onClick = {
                            if (selectedPdfUri == null) {
                                errorMessage = "Please select a PDF file"
                                return@PrimaryActionButton
                            }
                            isProcessing = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = viewModel.pdfOperations.splitPdf(selectedPdfUri!!, pageOrder, pdfName)
                                isProcessing = false
                                result.onSuccess { file ->
                                    createdFile = file
                                    viewModel.loadRecentFiles()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Failed to reorder PDF"
                                }
                            }
                        },
                        enabled = selectedPdfUri != null,
                        isLoading = isProcessing,
                        icon = Icons.Outlined.Reorder,
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
                SectionHeader(title = "SOURCE PDF")
                if (selectedPdfUri == null) {
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
                                text = "Select PDF Document",
                                color = colors.primary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (selectedPdfUri != null) {
                item {
                    SectionHeader(title = "PAGES (${pageOrder.size})")
                }

                itemsIndexed(pageOrder) { index, originalPageIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Position ${index + 1} → Page ${originalPageIndex + 1}",
                            color = colors.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        if (index > 0) {
                            IconButton(onClick = {
                                val item = pageOrder.removeAt(index)
                                pageOrder.add(index - 1, item)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = colors.secondary
                                )
                            }
                        }

                        if (index < pageOrder.size - 1) {
                            IconButton(onClick = {
                                val item = pageOrder.removeAt(index)
                                pageOrder.add(index + 1, item)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = colors.secondary
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
