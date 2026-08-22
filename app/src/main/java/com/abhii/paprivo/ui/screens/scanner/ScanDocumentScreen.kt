package com.abhii.paprivo.ui.screens.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.data.models.*
import com.abhii.paprivo.domain.scanner.DocumentScannerProcessor
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.PrimaryActionButton
import com.abhii.paprivo.ui.components.SecondaryActionButton
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun ScanDocumentScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val scannedPages = remember { mutableStateListOf<SelectedImage>() }
    var enhancementMode by remember { mutableStateOf(ScanEnhancement.COLOR) }
    var pdfName by remember { mutableStateOf("Scanned_${System.currentTimeMillis() / 1000}") }
    var isProcessing by remember { mutableStateOf(false) }
    var createdFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        coroutineScope.launch {
            for (uri in uris) {
                withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream)
                            if (bitmap != null) {
                                val enhanced = DocumentScannerProcessor.processImage(bitmap, enhancementMode, true)
                                val tempFile = File(context.cacheDir, "scan_${UUID.randomUUID()}.jpg")
                                FileOutputStream(tempFile).use { out ->
                                    enhanced.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                }
                                val tempUri = Uri.fromFile(tempFile)
                                scannedPages.add(
                                    SelectedImage(
                                        id = UUID.randomUUID().toString(),
                                        uri = tempUri,
                                        rotation = 0
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // ignore failed item
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Scan Document",
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
                        text = "Save as PDF (${scannedPages.size} Pages)",
                        onClick = {
                            if (scannedPages.isEmpty()) {
                                errorMessage = "Please scan or import at least one page"
                                return@PrimaryActionButton
                            }
                            isProcessing = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = viewModel.pdfOperations.createPdfFromImages(
                                    images = scannedPages,
                                    pageSize = PageSizeOption.A4,
                                    orientation = OrientationOption.PORTRAIT,
                                    margins = MarginsOption.NONE,
                                    quality = ImageQualityOption.HIGH,
                                    outputFileName = pdfName
                                )
                                isProcessing = false
                                result.onSuccess { file ->
                                    createdFile = file
                                    viewModel.loadRecentFiles()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Failed to generate scanned PDF"
                                }
                            }
                        },
                        enabled = scannedPages.isNotEmpty(),
                        isLoading = isProcessing,
                        icon = Icons.Outlined.DocumentScanner,
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
                        text = "SCANNED PAGES (${scannedPages.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = colors.secondary
                    )
                    TextButton(onClick = {
                        photoPickerLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp", "image/heic"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Scan / Add", color = colors.primary, fontSize = 13.sp)
                    }
                }
            }

            if (scannedPages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .clickable {
                                photoPickerLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp", "image/heic"))
                            }
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.DocumentScanner,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap to Scan / Import Document",
                                color = colors.primary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Auto perspective & contrast enhancement",
                                color = colors.secondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(scannedPages.size) { index ->
                    val item = scannedPages[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.iconContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P${index + 1}",
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scanned Page ${index + 1}",
                                color = colors.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Enhanced: ${enhancementMode.label}",
                                color = colors.secondary,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { scannedPages.removeAt(index) }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Remove",
                                tint = colors.secondary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "ENHANCEMENT FILTER")
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ScanEnhancement.values().size) { idx ->
                        val opt = ScanEnhancement.values()[idx]
                        FilterChip(
                            selected = enhancementMode == opt,
                            onClick = { enhancementMode = opt },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = if (colors.isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.primary
                            )
                        )
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
