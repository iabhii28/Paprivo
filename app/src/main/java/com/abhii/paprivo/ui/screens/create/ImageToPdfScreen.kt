package com.abhii.paprivo.ui.screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.abhii.paprivo.data.models.*
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.PrimaryActionButton
import com.abhii.paprivo.ui.components.SecondaryActionButton
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.navigation.Screen
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun ImageToPdfScreen(
    navController: NavController,
    viewModel: MainViewModel,
    imageTypeFilter: String? = null
) {
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    val selectedImages = remember { mutableStateListOf<SelectedImage>() }
    var pageSize by remember { mutableStateOf(PageSizeOption.A4) }
    var orientation by remember { mutableStateOf(OrientationOption.PORTRAIT) }
    var margins by remember { mutableStateOf(MarginsOption.NONE) }
    var quality by remember { mutableStateOf(ImageQualityOption.HIGH) }
    var pdfName by remember { mutableStateOf("Paprivo_${System.currentTimeMillis() / 1000}") }

    var isProcessing by remember { mutableStateOf(false) }
    var createdFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Native Storage Access Framework file picker with strict MIME types
    val mimeTypes = remember(imageTypeFilter) {
        when (imageTypeFilter?.lowercase()) {
            "jpg", "jpeg" -> arrayOf("image/jpeg")
            "png" -> arrayOf("image/png")
            "webp" -> arrayOf("image/webp")
            "heic" -> arrayOf("image/heic")
            "heif" -> arrayOf("image/heif")
            else -> arrayOf("image/jpeg", "image/png", "image/webp", "image/heic", "image/heif")
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            createdFile = null
        }
        uris.forEach { uri ->
            selectedImages.add(
                SelectedImage(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    rotation = 0,
                    fileName = uri.lastPathSegment ?: "image"
                )
            )
        }
    }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = "Image to PDF",
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
                        text = "Create PDF (${selectedImages.size} Images)",
                        onClick = {
                            if (selectedImages.isEmpty()) {
                                errorMessage = "Please select at least one image"
                                return@PrimaryActionButton
                            }
                            isProcessing = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = viewModel.pdfOperations.createPdfFromImages(
                                    images = selectedImages,
                                    pageSize = pageSize,
                                    orientation = orientation,
                                    margins = margins,
                                    quality = quality,
                                    outputFileName = pdfName
                                )
                                isProcessing = false
                                result.onSuccess { file ->
                                    createdFile = file
                                    viewModel.loadRecentFiles()
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Failed to create PDF"
                                }
                            }
                        },
                        enabled = selectedImages.isNotEmpty(),
                        isLoading = isProcessing,
                        icon = Icons.Outlined.PictureAsPdf,
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
            // Selected Images Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECTED IMAGES (${selectedImages.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = colors.secondary
                    )
                    TextButton(onClick = { imagePickerLauncher.launch(mimeTypes) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Images", color = colors.primary, fontSize = 13.sp)
                    }
                }
            }

            if (selectedImages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .clickable { imagePickerLauncher.launch(mimeTypes) }
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap to select images",
                                color = colors.primary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Supported: JPG, PNG, WEBP, HEIC",
                                color = colors.secondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(selectedImages) { index, item ->
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
                        // Thumbnail
                        AsyncImage(
                            model = item.uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .rotate(item.rotation.toFloat()),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Image ${index + 1}",
                                color = colors.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Rotation: ${item.rotation}°",
                                color = colors.secondary,
                                fontSize = 12.sp
                            )
                        }

                        // Rotate Action
                        IconButton(onClick = {
                            selectedImages[index] = item.copy(rotation = (item.rotation + 90) % 360)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.RotateRight,
                                contentDescription = "Rotate",
                                tint = colors.secondary
                            )
                        }

                        // Remove Action
                        IconButton(onClick = { selectedImages.removeAt(index) }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Remove",
                                tint = colors.secondary
                            )
                        }
                    }
                }
            }

            // PDF Settings
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "PAGE SIZE")
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PageSizeOption.values().size) { idx ->
                        val opt = PageSizeOption.values()[idx]
                        FilterChip(
                            selected = pageSize == opt,
                            onClick = { pageSize = opt },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = if (colors.isDark) Color.Black else Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.primary
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "ORIENTATION")
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(OrientationOption.values().size) { idx ->
                        val opt = OrientationOption.values()[idx]
                        FilterChip(
                            selected = orientation == opt,
                            onClick = { orientation = opt },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = if (colors.isDark) Color.Black else Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.primary
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "MARGINS")
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MarginsOption.values().size) { idx ->
                        val opt = MarginsOption.values()[idx]
                        FilterChip(
                            selected = margins == opt,
                            onClick = { margins = opt },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = if (colors.isDark) Color.Black else Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.primary
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "IMAGE QUALITY")
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ImageQualityOption.values().size) { idx ->
                        val opt = ImageQualityOption.values()[idx]
                        FilterChip(
                            selected = quality == opt,
                            onClick = { quality = opt },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = if (colors.isDark) Color.Black else Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.primary
                            )
                        )
                    }
                }
            }

            // Output Filename
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

            if (createdFile != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "PDF Created Successfully",
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primary
                                )
                                Text(
                                    text = "${createdFile?.name} (${(createdFile?.length() ?: 0) / 1024} KB)",
                                    fontSize = 12.sp,
                                    color = colors.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
