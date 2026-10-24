package org.autojs.toolkit.ml

import android.graphics.Bitmap
import android.graphics.Rect
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer

/**
 * YOLOv8 目标检测器：基于 ONNX Runtime（Microsoft 维护，替代旧版 NCNN）。
 *
 * 支持加载 YOLOv8 ONNX 模型，对图片进行目标检测，返回检测框列表。
 */
class YoloV8Detector(private val modelPath: String, private val labels: List<String>) {

    data class Detection(
        val label: String,
        val score: Float,
        val bounds: Rect
    )

    private val env = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var inputWidth = 640
    private var inputHeight = 640
    private var confThreshold = 0.25f
    private var nmsThreshold = 0.45f

    init {
        loadModel(modelPath)
    }

    private fun loadModel(path: String) {
        val modelBytes = java.io.File(path).readBytes()
        val opts = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            setIntraOpNumThreads(4)
        }
        session = env.createSession(modelBytes, opts)
    }

    fun setConfThreshold(threshold: Float) { confThreshold = threshold }
    fun setNmsThreshold(threshold: Float) { nmsThreshold = threshold }

    /**
     * 对图片执行目标检测。
     */
    fun detect(bitmap: Bitmap): List<Detection> {
        val session = session ?: return emptyList()
        val scaled = scaleBitmap(bitmap, inputWidth, inputHeight)
        val inputData = preprocess(scaled)
        val inputShape = longArrayOf(1, 3, inputHeight.toLong(), inputWidth.toLong())
        val inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), inputShape)

        val output = session.run(mapOf(session.inputNames.first() to inputTensor))
        val outputTensor = output[0] as OnnxTensor
        val rawResults = outputTensor.value as Array<FloatArray>
        inputTensor.close()
        outputTensor.close()
        output.close()

        return postprocess(rawResults, bitmap.width, bitmap.height)
    }

    private fun preprocess(bitmap: Bitmap): FloatArray {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val data = FloatArray(3 * w * h)
        for (i in pixels.indices) {
            val px = pixels[i]
            data[i] = ((px shr 16 and 0xFF) / 255.0f - 0.485f) / 0.229f
            data[w * h + i] = ((px shr 8 and 0xFF) / 255.0f - 0.456f) / 0.224f
            data[2 * w * h + i] = ((px and 0xFF) / 255.0f - 0.406f) / 0.225f
        }
        return data
    }

    private fun postprocess(raw: Array<FloatArray>, origW: Int, origH: Int): List<Detection> {
        val detections = mutableListOf<Detection>()
        val numClasses = labels.size
        val scaleX = origW.toFloat() / inputWidth
        val scaleY = origH.toFloat() / inputHeight

        for (i in raw[0].indices step numClasses + 5) {
            val objConf = raw[0][i + 4]
            if (objConf < confThreshold) continue
            var maxClass = 0
            var maxScore = 0f
            for (c in 0 until numClasses) {
                val score = raw[0][i + 5 + c]
                if (score > maxScore) { maxScore = score; maxClass = c }
            }
            val confidence = objConf * maxScore
            if (confidence < confThreshold) continue
            val x = (raw[0][i] - raw[0][i + 2] / 2) * scaleX
            val y = (raw[0][i + 1] - raw[0][i + 3] / 2) * scaleY
            val w = raw[0][i + 2] * scaleX
            val h = raw[0][i + 3] * scaleY
            detections.add(Detection(
                labels[maxClass], confidence,
                Rect(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt())
            ))
        }
        return nms(detections)
    }

    private fun nms(detections: List<Detection>): List<Detection> {
        val sorted = detections.sortedByDescending { it.score }
        val kept = mutableListOf<Detection>()
        val suppressed = BooleanArray(sorted.size)
        for (i in sorted.indices) {
            if (suppressed[i]) continue
            kept.add(sorted[i])
            for (j in i + 1 until sorted.size) {
                if (suppressed[j]) continue
                if (iou(sorted[i].bounds, sorted[j].bounds) > nmsThreshold) suppressed[j] = true
            }
        }
        return kept
    }

    private fun iou(a: Rect, b: Rect): Float {
        val ix1 = maxOf(a.left, b.left)
        val iy1 = maxOf(a.top, b.top)
        val ix2 = minOf(a.right, b.right)
        val iy2 = minOf(a.bottom, b.bottom)
        val iw = maxOf(0, ix2 - ix1)
        val ih = maxOf(0, iy2 - iy1)
        val inter = iw * ih
        val union = a.width() * a.height() + b.width() * b.height() - inter
        return if (union > 0) inter.toFloat() / union else 0f
    }

    private fun scaleBitmap(src: Bitmap, w: Int, h: Int): Bitmap =
        Bitmap.createScaledBitmap(src, w, h, false)

    fun close() { session?.close() }
}
