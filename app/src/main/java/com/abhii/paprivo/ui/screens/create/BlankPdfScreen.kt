package com.abhii.paprivo.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.data.models.OrientationOption
import com.abhii.paprivo.data.models.PageSizeOption
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.PrimaryActionButton
import com.abhii.paprivo.ui.components.SecondaryActionButton
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun BlankPdfScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    var pageCount by remember { mutableFloatStateOf(1f) }
    var pageSize by remember { mutableStateOf(PageSizeOption.A4) }
    var orientation by remember { mutableStateOf(OrientationOption.PORTRAIT) }
    var pdfName by remember { mutableStateOf("Blank_${System.currentTimeMillis() / 1000}") }
    var isProcessing by remember { mutableStateOf(false) }
    var createdFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Blank PDF",
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
                        text = "Generate Blank PDF (${pageCount.toInt()} Pages)",
                        onClick = {
                            isProcessing = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = viewModel.pdfOperations.createBlankPdf(
                                    pageCount = pageCount.toInt(),
                                    pageSize = pageSize,
                                    orientation = orientation,
                                    outputFileName = pdfName
                                )
                                isProcessing = false
                                result.onSuccess { file ->
                                    createdFile = file
                                    viewModel.loadRecentFiles()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Failed to generate PDF"
                                }
                            }
                        },
                        isLoading = isProcessing,
                        icon = Icons.Outlined.NoteAdd,
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
                SectionHeader(title = "NUMBER OF PAGES: ${pageCount.toInt()}")
                Slider(
                    value = pageCount,
                    onValueChange = { pageCount = it },
                    valueRange = 1f..50f,
                    steps = 48,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.iconContainer
                    )
                )
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
