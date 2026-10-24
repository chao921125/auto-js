package org.autojs.feature.inspector

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import org.autojs.toolkit.accessibility.AccessibilityEngine
import org.autojs.toolkit.accessibility.AutoJsAccessibilityService

/**
 * 布局分析器：遍历无障碍节点树，提取控件信息。
 *
 * 替代旧版 automator/LayoutInspector.kt，
 * 基于新版 AccessibilityEngine 实现。
 */
class LayoutInspector {

    data class NodeInfo(
        val text: String,
        val id: String,
        val className: String,
        val desc: String,
        val bounds: Rect,
        val clickable: Boolean,
        val scrollable: Boolean,
        val enabled: Boolean,
        val depth: Int,
        val children: List<NodeInfo>
    ) {
        fun toSelectorCode(): String {
            val parts = mutableListOf<String>()
            if (text.isNotEmpty()) parts.add("auto.text(\"$text\")")
            else if (id.isNotEmpty()) parts.add("auto.id(\"$id\")")
            else if (desc.isNotEmpty()) parts.add("auto.desc(\"$desc\")")
            if (clickable) parts.add("auto.clickable()")
            return if (parts.isEmpty()) "auto.clazz(\"$className\")"
                   else parts.joinToString(".")
        }
    }

    /**
     * 获取当前窗口的完整节点树。
     */
    fun inspect(): NodeInfo? {
        val service = AutoJsAccessibilityService.instance ?: return null
        val root = service.rootInActiveWindow ?: return null
        return buildNodeInfo(root, 0)
    }

    /**
     * 获取指定坐标处的控件信息。
     */
    fun pickElement(x: Int, y: Int): NodeInfo? {
        val service = AutoJsAccessibilityService.instance ?: return null
        val root = service.rootInActiveWindow ?: return null
        return findNodeAt(root, x, y)?.let { buildNodeInfo(it, 0) }
    }

    /**
     * 将节点树转为扁平列表（用于 RecyclerView 展示）。
     */
    fun flatten(root: NodeInfo): List<Pair<Int, NodeInfo>> {
        val result = mutableListOf<Pair<Int, NodeInfo>>()
        fun walk(node: NodeInfo, level: Int) {
            result.add(level to node)
            node.children.forEach { walk(it, level + 1) }
        }
        walk(root, 0)
        return result
    }

    private fun buildNodeInfo(node: AccessibilityNodeInfo, depth: Int): NodeInfo {
        val children = (0 until node.childCount).mapNotNull {
            node.getChild(it)?.let { child -> buildNodeInfo(child, depth + 1) }
        }
        return NodeInfo(
            text = node.text?.toString() ?: "",
            id = node.viewIdResourceName ?: "",
            className = node.className?.toString() ?: "",
            desc = node.contentDescription?.toString() ?: "",
            bounds = Rect().also { node.getBoundsInScreen(it) },
            clickable = node.isClickable,
            scrollable = node.isScrollable,
            enabled = node.isEnabled,
            depth = depth,
            children = children
        )
    }

    private fun findNodeAt(node: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        if (!bounds.contains(x, y)) return null
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeAt(child, x, y)
            if (result != null) return result
        }
        return node
    }
}
