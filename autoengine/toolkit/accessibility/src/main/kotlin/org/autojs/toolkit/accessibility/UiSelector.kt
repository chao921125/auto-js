package org.autojs.toolkit.accessibility

/**
 * UI 选择器：链式条件构建，编译为谓词树。
 * 支持 text/id/desc/clazz/depth/drawingOrder 等条件。
 */
class UiSelector {
    private val conditions = mutableListOf<(NodeInfo) -> Boolean>()

    fun text(value: String): UiSelector { conditions.add { it.text == value }; return this }
    fun textContains(value: String): UiSelector { conditions.add { it.text?.contains(value) == true }; return this }
    fun textStartsWith(value: String): UiSelector { conditions.add { it.text?.startsWith(value) == true }; return this }
    fun textEndsWith(value: String): UiSelector { conditions.add { it.text?.endsWith(value) == true }; return this }

    fun id(value: String): UiSelector { conditions.add { it.resourceId == value }; return this }
    fun idContains(value: String): UiSelector { conditions.add { it.resourceId?.contains(value) == true }; return this }

    fun desc(value: String): UiSelector { conditions.add { it.desc == value }; return this }
    fun descContains(value: String): UiSelector { conditions.add { it.desc?.contains(value) == true }; return this }

    fun clazz(value: String): UiSelector { conditions.add { it.className == value }; return this }
    fun clazzContains(value: String): UiSelector { conditions.add { it.className?.contains(value) == true }; return this }

    fun depth(value: Int): UiSelector { conditions.add { it.depth == value }; return this }
    fun depthAtLeast(value: Int): UiSelector { conditions.add { it.depth >= value }; return this }

    fun clickable(value: Boolean = true): UiSelector { conditions.add { it.isClickable == value }; return this }
    fun scrollable(value: Boolean = true): UiSelector { conditions.add { it.isScrollable == value }; return this }
    fun checked(value: Boolean = true): UiSelector { conditions.add { it.isChecked == value }; return this }
    fun enabled(value: Boolean = true): UiSelector { conditions.add { it.isEnabled == value }; return this }

    fun bounds(rect: android.graphics.Rect): UiSelector { conditions.add { it.bounds == rect }; return this }

    /**
     * 编译为谓词：所有条件 AND 组合。
     */
    fun toPredicate(): (NodeInfo) -> Boolean = { node ->
        conditions.all { it(node) }
    }

    fun isEmpty(): Boolean = conditions.isEmpty()
}

/**
 * 查询结果包装：提供 findOne/findAll/waitFor/exists/each 等方法。
 */
class UiQuery(private val engine: AccessibilityEngine, private val selector: UiSelector) {

    fun findOne(timeoutMs: Long = 0): NodeInfo? {
        return engine.findOne(selector.toPredicate(), timeoutMs)
    }

    fun findAll(): List<NodeInfo> {
        return engine.query(selector.toPredicate())
    }

    fun waitFor(timeoutMs: Long = 5000): NodeInfo? {
        return findOne(timeoutMs)
    }

    fun exists(): Boolean = findOne(0) != null

    fun each(action: (NodeInfo) -> Unit) {
        findAll().forEach(action)
    }

    fun click(): Boolean = findOne(0)?.let { engine.click(it) } ?: false
    fun longClick(): Boolean = findOne(0)?.let { engine.gestureLongClick(it.bounds.centerX().toFloat(), it.bounds.centerY().toFloat()) } ?: false
    fun setText(text: String): Boolean = findOne(0)?.let { engine.setText(it, text) } ?: false
    fun scrollForward(): Boolean = findOne(0)?.let { engine.gestureSwipe(it.bounds.centerX().toFloat(), it.bounds.bottom.toFloat(), it.bounds.centerX().toFloat(), it.bounds.top.toFloat(), 300) } ?: false
    fun scrollBackward(): Boolean = findOne(0)?.let { engine.gestureSwipe(it.bounds.centerX().toFloat(), it.bounds.top.toFloat(), it.bounds.centerX().toFloat(), it.bounds.bottom.toFloat(), 300) } ?: false
}
