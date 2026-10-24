package org.autojs.toolkit.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 无障碍引擎：维护窗口快照、去抖合并事件、提供查询与操作接口。
 */
class AccessibilityEngine {

    private var service: AccessibilityService? = null
    private val handler = Handler(Looper.getMainLooper())
    private val snapshots = ConcurrentHashMap<Int, WindowSnapshot>()
    private val debounceMs = 300L
    private val pendingUpdates = ConcurrentHashMap<Int, Runnable>()

    fun onConnected(service: AccessibilityService) {
        this.service = service
    }

    fun onDisconnected() {
        service = null
        snapshots.clear()
    }

    fun onInterrupt() {
        // 服务被系统中断，可通知脚本层
    }

    fun onEvent(event: AccessibilityEvent) {
        val windowId = event.windowId
        // 去抖：延迟更新快照
        pendingUpdates[windowId]?.let { handler.removeCallbacks(it) }
        val updateTask = Runnable { updateSnapshot(windowId, event) }
        pendingUpdates[windowId] = updateTask
        handler.postDelayed(updateTask, debounceMs)
    }

    private fun updateSnapshot(windowId: Int, event: AccessibilityEvent) {
        val svc = service ?: return
        val root = svc.rootInActiveWindow ?: return
        val snapshot = buildSnapshot(root)
        snapshots[windowId] = snapshot
        pendingUpdates.remove(windowId)
    }

    private fun buildSnapshot(root: AccessibilityNodeInfo): WindowSnapshot {
        val nodes = mutableListOf<NodeInfo>()
        collectNodes(root, nodes, 0)
        return WindowSnapshot(root.windowId, nodes)
    }

    private fun collectNodes(node: AccessibilityNodeInfo, out: MutableList<NodeInfo>, depth: Int) {
        val info = NodeInfo(
            id = node.hashCode(),
            text = node.text?.toString(),
            resourceId = node.viewIdResourceName,
            desc = node.contentDescription?.toString(),
            className = node.className?.toString(),
            bounds = Rect().apply { node.getBoundsInScreen(this) },
            depth = depth,
            isClickable = node.isClickable,
            isScrollable = node.isScrollable,
            isChecked = node.isChecked,
            isEnabled = node.isEnabled,
            isFocusable = node.isFocusable,
            childCount = node.childCount,
            parentId = -1
        )
        out.add(info)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                collectNodes(child, out, depth + 1)
            }
        }
    }

    /**
     * 查询所有窗口快照中的节点。
     */
    fun query(predicate: (NodeInfo) -> Boolean): List<NodeInfo> {
        return snapshots.values.flatMap { it.nodes }.filter(predicate)
    }

    /**
     * 查找单个节点（带超时）。
     */
    fun findOne(predicate: (NodeInfo) -> Boolean, timeoutMs: Long = 0): NodeInfo? {
        if (timeoutMs <= 0) return query(predicate).firstOrNull()
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val result = query(predicate).firstOrNull()
            if (result != null) return result
            Thread.sleep(100)
        }
        return null
    }

    /**
     * 点击节点。
     */
    fun click(node: NodeInfo): Boolean {
        val svc = service ?: return false
        val liveNode = findLiveNode(node) ?: return false
        // 一级：ACTION_CLICK
        if (liveNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        // 二级：沿父链找 clickable 节点
        var parent = liveNode.parent
        while (parent != null) {
            if (parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            parent = parent.parent
        }
        // 三级兜底：dispatchGesture 点坐标
        val center = Rect().apply { liveNode.getBoundsInScreen(this) }
        return gestureClick(center.centerX().toFloat(), center.centerY().toFloat())
    }

    /**
     * 输入文本。
     */
    fun setText(node: NodeInfo, text: String): Boolean {
        val liveNode = findLiveNode(node) ?: return false
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        if (liveNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) return true
        // 兜底：剪贴板粘贴
        return false
    }

    /**
     * 手势点击（dispatchGesture）。
     */
    fun gestureClick(x: Float, y: Float): Boolean {
        val svc = service ?: return false
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return svc.dispatchGesture(gesture, null, null)
    }

    /**
     * 手势滑动。
     */
    fun gestureSwipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long): Boolean {
        val svc = service ?: return false
        val path = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return svc.dispatchGesture(gesture, null, null)
    }

    /**
     * 长按手势。
     */
    fun gestureLongClick(x: Float, y: Float): Boolean {
        val svc = service ?: return false
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 800)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return svc.dispatchGesture(gesture, null, null)
    }

    private fun findLiveNode(node: NodeInfo): AccessibilityNodeInfo? {
        val svc = service ?: return null
        val root = svc.rootInActiveWindow ?: return null
        return findNodeById(root, node)
    }

    private fun findNodeById(root: AccessibilityNodeInfo, target: NodeInfo): AccessibilityNodeInfo? {
        val rect = Rect().apply { root.getBoundsInScreen(this) }
        if (root.text?.toString() == target.text && rect == target.bounds) return root
        for (i in 0 until root.childCount) {
            root.getChild(i)?.let { child ->
                findNodeById(child, target)?.let { return it }
            }
        }
        return null
    }

    fun getSnapshot(windowId: Int): WindowSnapshot? = snapshots[windowId]
    fun getAllSnapshots(): Collection<WindowSnapshot> = snapshots.values
    fun clearSnapshots() = snapshots.clear()
}
