package org.autojs.toolkit.vision

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope
import org.autojs.core.api.ScriptObjectBuilder

/**
 * images 命名空间 API：截图、找图、找色、图片操作。
 */
class ImagesApiProvider : ApiProvider {
    override val name = "images"

    private val captureManager = ScreenCaptureManager()
    private val matcher = ImageMatcher()
    private val colorFinder = ColorFinder()

    override fun install(scope: ScriptScope) {
        scope.defineObject("images") {
            function("requestScreenCapture") { _ -> captureManager.isReady() }
            function("captureScreen") { _ -> captureManager.capture() }
            function("findImage") { args ->
                val src = args[0] as? android.graphics.Bitmap ?: return@function null
                val tpl = args[1] as? android.graphics.Bitmap ?: return@function null
                @Suppress("UNCHECKED_CAST")
                val opts = args.getOrNull(2) as? Map<String, Any?> ?: emptyMap()
                val region = (opts["region"] as? List<Int>)?.toIntArray()
                val threshold = (opts["threshold"] as? Number)?.toDouble() ?: 0.8
                matcher.findImage(src, tpl, region, threshold)
            }
            function("findColor") { args ->
                val bmp = args[0] as? android.graphics.Bitmap ?: return@function null
                val color = (args[1] as? Number)?.toInt() ?: return@function null
                @Suppress("UNCHECKED_CAST")
                val opts = args.getOrNull(2) as? Map<String, Any?> ?: emptyMap()
                val region = (opts["region"] as? List<Int>)?.toIntArray()
                val threshold = (opts["threshold"] as? Number)?.toInt() ?: 4
                colorFinder.findColor(bmp, color, region, threshold)
            }
            function("findMultiColors") { args ->
                val bmp = args[0] as? android.graphics.Bitmap ?: return@function null
                val firstColor = (args[1] as? Number)?.toInt() ?: return@function null
                @Suppress("UNCHECKED_CAST")
                val offsets = (args[2] as? List<List<Int>>)?.map { it.toIntArray() } ?: emptyList()
                colorFinder.findMultiColors(bmp, firstColor, offsets)
            }
            function("detectsColor") { args ->
                val bmp = args[0] as? android.graphics.Bitmap ?: return@function false
                val x = (args[1] as? Number)?.toInt() ?: 0
                val y = (args[2] as? Number)?.toInt() ?: 0
                val color = (args[3] as? Number)?.toInt() ?: 0
                colorFinder.detectsColor(bmp, x, y, color)
            }
            function("crop") { args ->
                val bmp = args[0] as? android.graphics.Bitmap ?: return@function null
                @Suppress("UNCHECKED_CAST")
                val region = (args[1] as? List<Int>)?.toIntArray() ?: return@function null
                ImageMatcher.crop(bmp, region)
            }
            function("resize") { args ->
                val bmp = args[0] as? android.graphics.Bitmap ?: return@function null
                val w = (args[1] as? Number)?.toInt() ?: 0
                val h = (args[2] as? Number)?.toInt() ?: 0
                ImageMatcher.resize(bmp, w, h)
            }
            function("pixel") { args ->
                val bmp = args[0] as? android.graphics.Bitmap ?: return@function 0
                val x = (args[1] as? Number)?.toInt() ?: 0
                val y = (args[2] as? Number)?.toInt() ?: 0
                ImageMatcher.pixel(bmp, x, y)
            }
            function("read") { args ->
                val path = args[0]?.toString() ?: return@function null
                try { android.graphics.BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
            }
            function("save") { args ->
                val bmp = args[0] as? android.graphics.Bitmap ?: return@function false
                val path = args[1]?.toString() ?: return@function false
                try {
                    bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, java.io.FileOutputStream(path)); true
                } catch (e: Exception) { false }
            }
            property("captureManager", captureManager)
        }
    }
}

/**
 * ocr 命名空间 API：文字识别。
 */
class OcrApiProvider : ApiProvider {
    override val name = "ocr"
    private val engine = OcrEngine()

    override fun install(scope: ScriptScope) {
        scope.defineObject("ocr") {
            function("recognize") { args ->
                val bmp = args[0] as? android.graphics.Bitmap ?: return@function emptyList<Any>()
                kotlinx.coroutines.runBlocking { engine.recognize(bmp) }
            }
            function("close") { _ -> engine.close() }
        }
    }
}
