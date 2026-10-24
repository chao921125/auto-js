package org.autojs.toolkit.accessibility

import android.graphics.Rect

/**
 * 窗口快照：轻量镜像，避免长期持有 AccessibilityNodeInfo。
 */
data class WindowSnapshot(
    val windowId: Int,
    val nodes: List<NodeInfo>
)

/**
 * 节点信息：从 AccessibilityNodeInfo 提取的不可变快照。
 */
data class NodeInfo(
    val id: Int,
    val text: String?,
    val resourceId: String?,
    val desc: String?,
    val className: String?,
    val bounds: Rect,
    val depth: Int,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val isChecked: Boolean,
    val isEnabled: Boolean,
    val isFocusable: Boolean,
    val childCount: Int,
    val parentId: Int
) {
    fun center(): Pair<Int, Int> = Pair(bounds.centerX(), bounds.centerY())
    fun width(): Int = bounds.width()
    fun height(): Int = bounds.height()
}
