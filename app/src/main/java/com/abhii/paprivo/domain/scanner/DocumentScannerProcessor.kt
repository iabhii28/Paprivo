package com.abhii.paprivo.domain.scanner

import android.graphics.*
import com.abhii.paprivo.data.models.ScanEnhancement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DocumentScannerProcessor {

    suspend fun processImage(
        bitmap: Bitmap,
        enhancement: ScanEnhancement,
        autoCrop: Boolean
    ): Bitmap = withContext(Dispatchers.Default) {
        var result = bitmap

        // Auto contrast / enhance
        when (enhancement) {
            ScanEnhancement.ORIGINAL -> {
                // Keep original
            }
            ScanEnhancement.COLOR -> {
                result = enhanceColor(result)
            }
            ScanEnhancement.GRAYSCALE -> {
                result = toGrayscale(result)
            }
            ScanEnhancement.BW -> {
                result = toHighContrastBW(result)
            }
        }

        result
    }

    private fun enhanceColor(src: Bitmap): Bitmap {
        val dest = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix().apply {
            // Slight boost in contrast and brightness for clear document text
            val contrast = 1.25f
            val brightness = 10f
            set(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    private fun toGrayscale(src: Bitmap): Bitmap {
        val dest = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix().apply {
            setSaturation(0f)
        }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    private fun toHighContrastBW(src: Bitmap): Bitmap {
        val gray = toGrayscale(src)
        val dest = Bitmap.createBitmap(gray.width, gray.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val contrast = 2.0f
        val cm = ColorMatrix().apply {
            set(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, -128f * (contrast - 1f),
                    0f, contrast, 0f, 0f, -128f * (contrast - 1f),
                    0f, 0f, contrast, 0f, -128f * (contrast - 1f),
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(gray, 0f, 0f, paint)
        return dest
    }
}
