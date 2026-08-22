package com.abhii.paprivo.data.models

import android.net.Uri

data class PdfFileItem(
    val id: String,
    val name: String,
    val uri: Uri,
    val size: Long = 0L,
    val pageCount: Int = 1,
    val modifiedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class SelectedImage(
    val id: String,
    val uri: Uri,
    val rotation: Int = 0,
    val fileName: String = ""
)

enum class PageSizeOption(val label: String, val widthPt: Float, val heightPt: Float) {
    A4("A4", 595f, 842f),
    LETTER("Letter", 612f, 792f),
    LEGAL("Legal", 612f, 1008f),
    ORIGINAL("Original", 0f, 0f)
}

enum class OrientationOption(val label: String) {
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape"),
    AUTO("Auto")
}

enum class MarginsOption(val label: String, val paddingPt: Float) {
    NONE("None", 0f),
    SMALL("Small", 16f),
    MEDIUM("Medium", 32f),
    LARGE("Large", 48f)
}

enum class ImageQualityOption(val label: String, val compressionQuality: Int) {
    LOW("Low", 40),
    MEDIUM("Medium", 70),
    HIGH("High", 90),
    ORIGINAL("Original", 100)
}

enum class CompressionLevel(val label: String, val sampleScale: Float, val jpegQuality: Int, val estimate: String) {
    MAXIMUM("Maximum Compression", 0.5f, 40, "Up to 70% reduction"),
    BALANCED("Balanced", 0.75f, 70, "Up to 40% reduction"),
    HIGH_QUALITY("High Quality", 0.9f, 85, "Up to 15% reduction")
}

enum class ScanEnhancement(val label: String) {
    ORIGINAL("Original"),
    COLOR("Color"),
    GRAYSCALE("Grayscale"),
    BW("B & W")
}

enum class ThemeMode(val label: String) {
    BLACK("Black (AMOLED)"),
    WHITE("White (Clean)")
}

data class AppActivityLog(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
