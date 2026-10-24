package org.autojs.toolkit.floaty

import android.content.Context
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.xmlpull.v1.XmlPullParser

/**
 * XML → View 渲染器：将脚本内 XML 字符串解析为原生 View。
 * 用于 floaty.window(xml) 和 ui.layout(xml)。
 */
class XmlViewRenderer(private val context: Context) {

    /**
     * 解析 XML 字符串为 View 树。
     * 支持标准 Android View 标签 + autojs 扩展属性。
     */
    fun render(xml: String, parent: ViewGroup? = null): View {
        val parser = Xml.newPullParser()
        parser.setInput(xml.reader())
        return parseView(parser, parent)
    }

    private fun parseView(parser: XmlPullParser, parent: ViewGroup?): View {
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name
                    val view = createView(tagName)
                    // 解析属性
                    for (i in 0 until parser.attributeCount) {
                        val attrName = parser.getAttributeName(i)
                        val attrValue = parser.getAttributeValue(i)
                        applyAttribute(view, attrName, attrValue)
                    }
                    // 递归解析子元素
                    if (view is ViewGroup) {
                        parseChildren(parser, view)
                    } else {
                        skipChildren(parser)
                    }
                    return view
                }
            }
            event = parser.next()
        }
        throw IllegalArgumentException("No root view found in XML")
    }

    private fun parseChildren(parser: XmlPullParser, parent: ViewGroup) {
        var event = parser.next()
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val child = parseView(parser, parent)
                    parent.addView(child)
                }
                XmlPullParser.END_TAG -> return
            }
            event = parser.next()
        }
    }

    private fun skipChildren(parser: XmlPullParser) {
        var depth = 1
        while (depth > 0) {
            val event = parser.next()
            when (event) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    private fun createView(tagName: String): View {
        // 映射常见标签到 Android View 类
        val className = when (tagName) {
            "frame" -> "android.widget.FrameLayout"
            "linear" -> "android.widget.LinearLayout"
            "relative" -> "android.widget.RelativeLayout"
            "text" -> "android.widget.TextView"
            "button" -> "android.widget.Button"
            "img" -> "android.widget.ImageView"
            "input" -> "android.widget.EditText"
            "checkbox" -> "android.widget.CheckBox"
            "scroll" -> "android.widget.ScrollView"
            "list" -> "android.widget.ListView"
            else -> "android.view.View"
        }
        return LayoutInflater.from(context).inflate(
            context.resources.getIdentifier(tagName, "layout", context.packageName).takeIf { it != 0 } ?: 0,
            null
        ).let {
            // fallback: 反射创建
            try {
                Class.forName(className).getConstructor(Context::class.java).newInstance(context) as View
            } catch (e: Exception) {
                android.widget.TextView(context)
            }
        }
    }

    private fun applyAttribute(view: View, name: String, value: String) {
        when (name) {
            "text" -> (view as? android.widget.TextView)?.text = value
            "w", "width" -> {} // LayoutParams 处理
            "h", "height" -> {}
            "padding" -> view.setPadding(value.toInt(), value.toInt(), value.toInt(), value.toInt())
            "bg", "background" -> {
                if (value.startsWith("#")) view.setBackgroundColor(android.graphics.Color.parseColor(value))
            }
            "gravity" -> (view as? android.widget.TextView)?.gravity = parseGravity(value)
            // autojs 扩展属性
            "onClick" -> view.tag = mapOf("onClick" to value)
            "textColor" -> (view as? android.widget.TextView)?.setTextColor(android.graphics.Color.parseColor(value))
        }
    }

    private fun parseGravity(value: String): Int {
        var g = 0
        if (value.contains("center")) g = g or android.view.Gravity.CENTER
        if (value.contains("left")) g = g or android.view.Gravity.LEFT
        if (value.contains("right")) g = g or android.view.Gravity.RIGHT
        if (value.contains("top")) g = g or android.view.Gravity.TOP
        if (value.contains("bottom")) g = g or android.view.Gravity.BOTTOM
        return g
    }
}
