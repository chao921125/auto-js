package org.autojs.toolkit.vision

import android.graphics.*
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.content.Context
import android.content.Intent
import android.hardware.display.VirtualDisplay
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

/**
 * 截图通道抽象：三实现自动择优（MediaProjection / Accessibility takeScreenshot / Root）。
 */
interface ScreenCapturer {
    fun init(): Boolean
    fun capture(): Bitmap?
    fun release()
    val isReady: Boolean
}

/**
 * MediaProjection 截图通道：主通道，前台服务内使用。
 */
class MediaProjectionCapturer(
    private val context: Context,
    private val resultCode: Int,
    private val resultData: Intent
) : ScreenCapturer {

    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var width = 0
    private var height = 0
    @Volatile private var ready = false

    override val isReady: Boolean get() = ready

    override fun init(): Boolean {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(metrics)
        width = metrics.widthPixels
        height = metrics.heightPixels

        val mgr = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mgr.getMediaProjection(resultCode, resultData)
        projection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { ready = false }
        }, null)

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = projection?.createVirtualDisplay(
            "AutoJsScreen", width, height, metrics.densityDpi,
            0x10, imageReader!!.surface, null, null
        )
        ready = true
        return true
    }

    override fun capture(): Bitmap? {
        val reader = imageReader ?: return null
        val image = reader.acquireLatestImage() ?: return null
        return imageToBitmap(image)
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val bmp = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bmp.copyPixelsFromBuffer(buffer)
        return if (rowPadding > 0) Bitmap.createBitmap(bmp, 0, 0, width, height) else bmp
    }

    override fun release() {
        virtualDisplay?.release()
        imageReader?.close()
        projection?.stop()
        virtualDisplay = null; imageReader = null; projection = null
        ready = false
    }
}

/**
 * 截图管理器：统一管理截图通道生命周期。
 */
class ScreenCaptureManager {
    private var capturer: ScreenCapturer? = null

    fun requestCapture(context: Context, resultCode: Int, data: Intent): Boolean {
        capturer = MediaProjectionCapturer(context, resultCode, data)
        return capturer!!.init()
    }

    fun capture(): Bitmap? = capturer?.capture()

    fun isReady(): Boolean = capturer?.isReady == true

    fun release() {
        capturer?.release()
        capturer = null
    }
}
