package com.abhii.paprivo.domain.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.abhii.paprivo.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import kotlin.math.max
import kotlin.math.min

class PdfOperationsManager(private val context: Context) {

    val outputDirectory: File
        get() = getOutputDir()

    private fun getOutputDir(): File {
        val dir = File(context.filesDir, "pdfs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getThumbnailsDir(): File {
        val dir = File(context.cacheDir, "thumbnails")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun createPdfFromImages(
        images: List<SelectedImage>,
        pageSize: PageSizeOption,
        orientation: OrientationOption,
        margins: MarginsOption,
        quality: ImageQualityOption,
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (images.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No images provided"))
            }

            val pdfDocument = PdfDocument()
            val cleanName = if (outputFileName.endsWith(".pdf", ignoreCase = true)) outputFileName else "$outputFileName.pdf"
            val outputFile = File(getOutputDir(), cleanName)

            for (i in images.indices) {
                val item = images[i]
                var bitmap = loadBitmapFromUri(item.uri) ?: continue

                // Rotate if needed
                if (item.rotation % 360 != 0) {
                    val matrix = Matrix().apply { postRotate(item.rotation.toFloat()) }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }

                // Determine Page Dimensions in Points (72 DPI)
                var pageWidth: Float
                var pageHeight: Float

                if (pageSize == PageSizeOption.ORIGINAL) {
                    pageWidth = bitmap.width.toFloat()
                    pageHeight = bitmap.height.toFloat()
                } else {
                    val baseW = pageSize.widthPt
                    val baseH = pageSize.heightPt

                    when (orientation) {
                        OrientationOption.PORTRAIT -> {
                            pageWidth = min(baseW, baseH)
                            pageHeight = max(baseW, baseH)
                        }
                        OrientationOption.LANDSCAPE -> {
                            pageWidth = max(baseW, baseH)
                            pageHeight = min(baseW, baseH)
                        }
                        OrientationOption.AUTO -> {
                            if (bitmap.width > bitmap.height) {
                                pageWidth = max(baseW, baseH)
                                pageHeight = min(baseW, baseH)
                            } else {
                                pageWidth = min(baseW, baseH)
                                pageHeight = max(baseW, baseH)
                            }
                        }
                    }
                }

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), i + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Fill clean white background
                val bgPaint = Paint().apply { color = Color.WHITE }
                canvas.drawRect(0f, 0f, pageWidth, pageHeight, bgPaint)

                val padding = margins.paddingPt
                val availableW = max(1f, pageWidth - (padding * 2))
                val availableH = max(1f, pageHeight - (padding * 2))

                // Scale image into available bounds
                val scale = min(availableW / bitmap.width.toFloat(), availableH / bitmap.height.toFloat())
                val destW = bitmap.width.toFloat() * scale
                val destH = bitmap.height.toFloat() * scale
                val destX = padding + (availableW - destW) / 2f
                val destY = padding + (availableH - destH) / 2f

                val destRect = RectF(destX, destY, destX + destW, destY + destH)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

                canvas.drawBitmap(bitmap, null, destRect, paint)
                pdfDocument.finishPage(page)
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBlankPdf(
        pageCount: Int,
        pageSize: PageSizeOption,
        orientation: OrientationOption,
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val cleanName = if (outputFileName.endsWith(".pdf", ignoreCase = true)) outputFileName else "$outputFileName.pdf"
            val outputFile = File(getOutputDir(), cleanName)

            val baseW = if (pageSize == PageSizeOption.ORIGINAL) 595f else pageSize.widthPt
            val baseH = if (pageSize == PageSizeOption.ORIGINAL) 842f else pageSize.heightPt

            val pageWidth = if (orientation == OrientationOption.LANDSCAPE) max(baseW, baseH) else min(baseW, baseH)
            val pageHeight = if (orientation == OrientationOption.LANDSCAPE) min(baseW, baseH) else max(baseW, baseH)

            for (i in 1..pageCount) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), i).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)
                pdfDocument.finishPage(page)
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTextToPdf(
        text: String,
        fontSize: Float,
        pageSize: PageSizeOption,
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val cleanName = if (outputFileName.endsWith(".pdf", ignoreCase = true)) outputFileName else "$outputFileName.pdf"
            val outputFile = File(getOutputDir(), cleanName)

            val pageWidth = pageSize.widthPt.toInt().coerceAtLeast(400)
            val pageHeight = pageSize.heightPt.toInt().coerceAtLeast(600)
            val margin = 40f

            val paint = Paint().apply {
                color = Color.BLACK
                textSize = fontSize
                isAntiAlias = true
                typeface = Typeface.SANS_SERIF
            }

            val textWidth = pageWidth - (margin * 2)
            val lines = mutableListOf<String>()

            text.lines().forEach { rawLine ->
                if (rawLine.isEmpty()) {
                    lines.add("")
                } else {
                    var currentLine = ""
                    val words = rawLine.split(" ")
                    for (word in words) {
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paint.measureText(testLine) <= textWidth) {
                            currentLine = testLine
                        } else {
                            if (currentLine.isNotEmpty()) lines.add(currentLine)
                            currentLine = word
                        }
                    }
                    if (currentLine.isNotEmpty()) lines.add(currentLine)
                }
            }

            val lineHeight = fontSize * 1.5f
            val maxLinesPerPage = ((pageHeight - (margin * 2)) / lineHeight).toInt().coerceAtLeast(1)

            var lineIndex = 0
            var pageNumber = 1

            while (lineIndex < lines.size) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                var y = margin + fontSize
                var linesOnThisPage = 0

                while (lineIndex < lines.size && linesOnThisPage < maxLinesPerPage) {
                    val line = lines[lineIndex]
                    canvas.drawText(line, margin, y, paint)
                    y += lineHeight
                    linesOnThisPage++
                    lineIndex++
                }

                pdfDocument.finishPage(page)
                pageNumber++
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun mergePdfs(pdfUris: List<Uri>, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val cleanName = if (outputFileName.endsWith(".pdf", ignoreCase = true)) outputFileName else "$outputFileName.pdf"
            val outputFile = File(getOutputDir(), cleanName)

            var globalPageIndex = 1

            for (uri in pdfUris) {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: continue
                val renderer = PdfRenderer(pfd)

                for (pageIdx in 0 until renderer.pageCount) {
                    val page = renderer.openPage(pageIdx)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, globalPageIndex).create()
                    val newPage = pdfDocument.startPage(pageInfo)
                    val canvas = newPage.canvas
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(newPage)

                    globalPageIndex++
                }
                renderer.close()
                pfd.close()
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun splitPdf(
        pdfUri: Uri,
        pageIndices: List<Int>, // 0-based
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: return@withContext Result.failure(IllegalStateException("Cannot open PDF file"))
            val renderer = PdfRenderer(pfd)

            val pdfDocument = PdfDocument()
            val cleanName = if (outputFileName.endsWith(".pdf", ignoreCase = true)) outputFileName else "$outputFileName.pdf"
            val outputFile = File(getOutputDir(), cleanName)

            var targetPageNumber = 1
            for (idx in pageIndices) {
                if (idx in 0 until renderer.pageCount) {
                    val page = renderer.openPage(idx)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, targetPageNumber).create()
                    val newPage = pdfDocument.startPage(pageInfo)
                    val canvas = newPage.canvas
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(newPage)
                    targetPageNumber++
                }
            }

            renderer.close()
            pfd.close()

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun compressPdf(
        pdfUri: Uri,
        level: CompressionLevel,
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: return@withContext Result.failure(IllegalStateException("Cannot open PDF file"))
            val renderer = PdfRenderer(pfd)

            val pdfDocument = PdfDocument()
            val cleanName = if (outputFileName.endsWith(".pdf", ignoreCase = true)) outputFileName else "$outputFileName.pdf"
            val outputFile = File(getOutputDir(), cleanName)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val targetW = (page.width * level.sampleScale).toInt().coerceAtLeast(100)
                val targetH = (page.height * level.sampleScale).toInt().coerceAtLeast(100)

                val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.RGB_565)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                page.close()

                // Compress bitmap with JPEG stream
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, level.jpegQuality, stream)
                val compressedBytes = stream.toByteArray()
                val compressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

                val pageInfo = PdfDocument.PageInfo.Builder(targetW, targetH, i + 1).create()
                val newPage = pdfDocument.startPage(pageInfo)
                val canvas = newPage.canvas
                canvas.drawBitmap(compressedBitmap, 0f, 0f, null)
                pdfDocument.finishPage(newPage)
            }

            renderer.close()
            pfd.close()

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rotatePdf(
        pdfUri: Uri,
        rotationAngle: Int,
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: return@withContext Result.failure(IllegalStateException("Cannot open PDF"))
            val renderer = PdfRenderer(pfd)

            val pdfDocument = PdfDocument()
            val cleanName = if (outputFileName.endsWith(".pdf", ignoreCase = true)) outputFileName else "$outputFileName.pdf"
            val outputFile = File(getOutputDir(), cleanName)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                val pageInfo = PdfDocument.PageInfo.Builder(rotatedBitmap.width, rotatedBitmap.height, i + 1).create()
                val newPage = pdfDocument.startPage(pageInfo)
                val canvas = newPage.canvas
                canvas.drawBitmap(rotatedBitmap, 0f, 0f, null)
                pdfDocument.finishPage(newPage)
            }

            renderer.close()
            pfd.close()

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun convertPdfToImages(pdfUri: Uri): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: return@withContext Result.failure(IllegalStateException("Cannot open PDF"))
            val renderer = PdfRenderer(pfd)

            val imageFiles = mutableListOf<File>()
            val exportDir = File(context.cacheDir, "pdf_images_${System.currentTimeMillis()}")
            exportDir.mkdirs()

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val imageFile = File(exportDir, "page_${i + 1}.png")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                imageFiles.add(imageFile)
            }

            renderer.close()
            pfd.close()

            Result.success(imageFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renderPageToBitmap(pdfUri: Uri, pageIndex: Int, scale: Float = 1.5f): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return@withContext null
            val renderer = PdfRenderer(pfd)
            if (pageIndex !in 0 until renderer.pageCount) {
                renderer.close()
                pfd.close()
                return@withContext null
            }

            val page = renderer.openPage(pageIndex)
            val w = (page.width * scale).toInt().coerceAtLeast(1)
            val h = (page.height * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            pfd.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPdfPageCount(pdfUri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return@withContext 0
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            renderer.close()
            pfd.close()
            count
        } catch (e: Exception) {
            0
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }
}
