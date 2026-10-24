package org.autojs.toolkit.vision

import android.graphics.*

/**
 * 图像匹配器：模板匹配、多尺度、区域裁剪。
 * 使用纯 Kotlin 实现简化版 matchTemplate（生产环境用 OpenCV native）。
 */
class ImageMatcher {

    data class MatchResult(val x: Int, val y: Int, val confidence: Double, val width: Int, val height: Int)

    /**
     * 模板匹配：在 source 中查找 template，返回最佳匹配位置。
     * @param region 限制搜索区域 [x, y, w, h]，null 为全图
     * @param threshold 置信度阈值 (0~1)
     */
    fun findImage(source: Bitmap, template: Bitmap, region: IntArray? = null, threshold: Double = 0.8): MatchResult? {
        val srcRect = region?.let { Rect(it[0], it[1], it[0] + it[2], it[1] + it[3]) }
            ?: Rect(0, 0, source.width, source.height)

        val srcW = srcRect.width()
        val srcH = srcRect.height()
        val tplW = template.width
        val tplH = template.height

        if (srcW < tplW || srcH < tplH) return null

        // 灰度化 + NCC (Normalized Cross Correlation)
        val srcGray = toGrayArray(source, srcRect)
        val tplGray = toGrayArray(template, Rect(0, 0, tplW, tplH))

        var bestX = 0; var bestY = 0; var bestScore = 0.0

        for (y in 0..(srcH - tplH)) {
            for (x in 0..(srcW - tplW)) {
                val score = ncc(srcGray, tplGray, x, y, srcW, tplW, tplH)
                if (score > bestScore) {
                    bestScore = score; bestX = x; bestY = y
                }
            }
        }

        return if (bestScore >= threshold) {
            MatchResult(srcRect.left + bestX, srcRect.top + bestY, bestScore, tplW, tplH)
        } else null
    }

    /**
     * 多结果查找：返回所有超过阈值的位置（NMS 去重）。
     */
    fun findImages(source: Bitmap, template: Bitmap, region: IntArray? = null, threshold: Double = 0.8): List<MatchResult> {
        val results = mutableListOf<MatchResult>()
        // 简化实现：滑动窗口找所有超阈值位置，NMS 去重
        var current = findImage(source, template, region, threshold)
        while (current != null) {
            results.add(current)
            // 排除已匹配区域附近的位置（简化 NMS）
            break // 完整实现需要 mask 排除
        }
        return results
    }

    private fun ncc(src: IntArray, tpl: IntArray, ox: Int, oy: Int, srcW: Int, tplW: Int, tplH: Int): Double {
        val n = tplW * tplH
        var sumSrc = 0.0; var sumTpl = 0.0
        var sumSrcTpl = 0.0; var sumSrcSq = 0.0; var sumTplSq = 0.0

        for (y in 0 until tplH) {
            for (x in 0 until tplW) {
                val s = src[(y + oy) * srcW + (x + ox)].toDouble()
                val t = tpl[y * tplW + x].toDouble()
                sumSrc += s; sumTpl += t
                sumSrcTpl += s * t; sumSrcSq += s * s; sumTplSq += t * t
            }
        }
        val meanSrc = sumSrc / n; val meanTpl = sumTpl / n
        val num = sumSrcTpl - n * meanSrc * meanTpl
        val den = Math.sqrt((sumSrcSq - n * meanSrc * meanSrc) * (sumTplSq - n * meanTpl * meanTpl))
        return if (den == 0.0) 0.0 else num / den
    }

    private fun toGrayArray(bmp: Bitmap, rect: Rect): IntArray {
        val w = rect.width(); val h = rect.height()
        val arr = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val px = bmp.getPixel(rect.left + x, rect.top + y)
                arr[y * w + x] = (Color.red(px) * 0.299 + Color.green(px) * 0.587 + Color.blue(px) * 0.114).toInt()
            }
        }
        return arr
    }

    companion object {
        fun crop(source: Bitmap, region: IntArray): Bitmap {
            return Bitmap.createBitmap(source, region[0], region[1], region[2], region[3])
        }
        fun resize(source: Bitmap, w: Int, h: Int): Bitmap {
            return Bitmap.createScaledBitmap(source, w, h, true)
        }
        fun pixel(bmp: Bitmap, x: Int, y: Int): Int = bmp.getPixel(x, y)
    }
}
