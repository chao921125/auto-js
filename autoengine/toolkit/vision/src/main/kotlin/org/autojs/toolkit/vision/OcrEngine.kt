package org.autojs.toolkit.vision

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * OCR 引擎：基于 ML Kit Text Recognition v2（中文离线）。
 */
class OcrEngine {

    data class OcrItem(val text: String, val bounds: Rect, val confidence: Float? = null)
    data class OcrResult(val items: List<OcrItem>) {
        val fullText: String get() = items.joinToString("\n") { it.text }
    }

    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    /**
     * 识别图片中的文字（异步）。
     */
    suspend fun recognize(bitmap: Bitmap, region: IntArray? = null): OcrResult {
        val image = if (region != null) {
            InputImage.fromBitmap(ImageMatcher.crop(bitmap, region), 0)
        } else {
            InputImage.fromBitmap(bitmap, 0)
        }
        return suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val items = visionText.textBlocks.flatMap { block ->
                        block.lines.map { line ->
                            OcrItem(line.text, line.boundingBox ?: Rect(), null)
                        }
                    }
                    cont.resume(OcrResult(items))
                }
                .addOnFailureListener { cont.resume(OcrResult(emptyList())) }
        }
    }

    fun close() = recognizer.close()
}
