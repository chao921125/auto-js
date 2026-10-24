package org.autojs.toolkit.vision

import android.graphics.*

/**
 * 颜色查找器：findColor / findMultiColors / detectsColor。
 * 区域内逐像素比对，支持阈值容差。
 */
class ColorFinder {

    data class ColorResult(val x: Int, val y: Int)

    /**
     * 找色：在图片中查找指定颜色。
     * @param region [x, y, w, h]，null 为全图
     * @param threshold 颜色容差 (0~255)
     */
    fun findColor(bmp: Bitmap, color: Int, region: IntArray? = null, threshold: Int = 4): ColorResult? {
        val r = region ?: intArrayOf(0, 0, bmp.width, bmp.height)
        val targetR = Color.red(color); val targetG = Color.green(color); val targetB = Color.blue(color)
        for (y in r[1] until (r[1] + r[3])) {
            for (x in r[0] until (r[0] + r[2])) {
                val px = bmp.getPixel(x, y)
                if (colorMatch(px, targetR, targetG, targetB, threshold)) return ColorResult(x, y)
            }
        }
        return null
    }

    /**
     * 多点找色：先找主色，再验证相对偏移位置的颜色是否匹配。
     * @param firstColor 主色
     * @param offsetColors 相对偏移颜色列表 [[dx, dy, color], ...]
     */
    fun findMultiColors(bmp: Bitmap, firstColor: Int, offsetColors: List<IntArray>, region: IntArray? = null, threshold: Int = 4): ColorResult? {
        val r = region ?: intArrayOf(0, 0, bmp.width, bmp.height)
        val main = findColor(bmp, firstColor, r, threshold) ?: return null
        for (offset in offsetColors) {
            val ox = main.x + offset[0]; val oy = main.y + offset[1]
            if (ox < 0 || oy < 0 || ox >= bmp.width || oy >= bmp.height) return null
            if (!colorMatch(bmp.getPixel(ox, oy), Color.red(offset[2]), Color.green(offset[2]), Color.blue(offset[2]), threshold)) return null
        }
        return main
    }

    /**
     * 检测指定坐标颜色是否匹配。
     */
    fun detectsColor(bmp: Bitmap, x: Int, y: Int, color: Int, threshold: Int = 4): Boolean {
        if (x < 0 || y < 0 || x >= bmp.width || y >= bmp.height) return false
        return colorMatch(bmp.getPixel(x, y), Color.red(color), Color.green(color), Color.blue(color), threshold)
    }

    private fun colorMatch(px: Int, r: Int, g: Int, b: Int, threshold: Int): Boolean {
        return Math.abs(Color.red(px) - r) <= threshold &&
               Math.abs(Color.green(px) - g) <= threshold &&
               Math.abs(Color.blue(px) - b) <= threshold
    }
}
