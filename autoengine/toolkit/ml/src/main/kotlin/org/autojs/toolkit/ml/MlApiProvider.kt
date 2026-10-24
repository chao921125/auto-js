package org.autojs.toolkit.ml

import android.graphics.Bitmap
import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope

/**
 * ml 命名空间 API：YOLOv8 目标检测。
 *
 * 用法：
 *   let detector = ml.createDetector("/sdcard/yolov8.onnx", ["person", "car"])
 *   let results = detector.detect(img)
 *   detector.release()
 */
class MlApiProvider : ApiProvider {
    override val name = "ml"

    override fun install(scope: ScriptScope) {
        scope.defineObject("ml") {
            function("createDetector") { args ->
                val modelPath = args[0]?.toString() ?: return@function null
                @Suppress("UNCHECKED_CAST")
                val labels = (args[1] as? List<String>) ?: emptyList()
                val conf = (args.getOrNull(2) as? Number)?.toFloat() ?: 0.25f
                val nms = (args.getOrNull(3) as? Number)?.toFloat() ?: 0.45f
                try {
                    YoloV8Detector(modelPath, labels).apply {
                        setConfThreshold(conf)
                        setNmsThreshold(nms)
                    }
                } catch (e: Exception) { null }
            }
            function("detect") { args ->
                val detector = args[0] as? YoloV8Detector ?: return@function emptyList<Any>()
                val bmp = args[1] as? Bitmap ?: return@function emptyList<Any>()
                detector.detect(bmp).map { d ->
                    mapOf(
                        "label" to d.label,
                        "score" to d.score,
                        "bounds" to listOf(d.bounds.left, d.bounds.top, d.bounds.right, d.bounds.bottom)
                    )
                }
            }
            function("release") { args ->
                val detector = args[0] as? YoloV8Detector ?: return@function false
                detector.close(); true
            }
        }
    }
}
