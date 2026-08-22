package com.abhii.paprivo.ui.screens.viewer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abhii.paprivo.ui.components.PaprivoTopBar
import com.abhii.paprivo.ui.components.SectionHeader
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun PdfViewerScreen(
    navController: NavController,
    viewModel: MainViewModel,
    initialUri: String? = null
) {
    val context = LocalContext.current
    val colors = LocalPaprivoColors.current
    val coroutineScope = rememberCoroutineScope()

    var pdfUri by remember { mutableStateOf<Uri?>(if (initialUri != null) Uri.parse(initialUri) else null) }
    var totalPages by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offsetX += offsetChange.x
        offsetY += offsetChange.y
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pdfUri = uri
            currentPageIndex = 0
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }
    }

    LaunchedEffect(pdfUri) {
        if (pdfUri != null) {
            totalPages = viewModel.pdfOperations.getPdfPageCount(pdfUri!!)
            if (totalPages > 0) {
                isLoadingPage = true
                currentBitmap = viewModel.pdfOperations.renderPageToBitmap(pdfUri!!, currentPageIndex)
                isLoadingPage = false
            }
        }
    }

    LaunchedEffect(currentPageIndex) {
        if (pdfUri != null && totalPages > 0) {
            isLoadingPage = true
            currentBitmap = viewModel.pdfOperations.renderPageToBitmap(pdfUri!!, currentPageIndex)
            isLoadingPage = false
        }
    }

    Scaffold(
        topBar = {
            PaprivoTopBar(
                title = if (pdfUri != null) "Page ${currentPageIndex + 1} of $totalPages" else "PDF Viewer",
                onBackClick = { navController.popBackStack() },
                actions = {
                    if (pdfUri != null) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, pdfUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share PDF"))
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = colors.primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (pdfUri != null && totalPages > 1) {
                Surface(
                    color = colors.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentPageIndex > 0) {
                                    currentPageIndex--
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous Page",
                                tint = if (currentPageIndex > 0) colors.primary else colors.secondary
                            )
                        }

                        Text(
                            text = "${currentPageIndex + 1} / $totalPages",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary
                        )

                        IconButton(
                            onClick = {
                                if (currentPageIndex < totalPages - 1) {
                                    currentPageIndex++
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            },
                            enabled = currentPageIndex < totalPages - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next Page",
                                tint = if (currentPageIndex < totalPages - 1) colors.primary else colors.secondary
                            )
                        }
                    }
                }
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            if (pdfUri == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .clickable { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Open PDF Document",
                            color = colors.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to select and read",
                            color = colors.secondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (isLoadingPage) {
                CircularProgressIndicator(
                    color = colors.primary,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 2.dp
                )
            } else if (currentBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .transformable(state = transformState)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page ${currentPageIndex + 1}",
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
