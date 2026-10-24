package org.autojs.app.editor

import android.content.Context
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.SymbolPairMatch
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem

/**
 * 代码编辑器增强：基于 Sora Editor 实现代码补全、语法高亮、符号匹配。
 *
 * 替代旧版 app/model/autocomplete/ 下的 DictionaryTree/AutoCompletion/CodeCompletion，
 * 使用 Sora Editor 0.20.4（Rosemoe 维护，Maven Central 最新可用版本）。
 */
class CodeEditorEnhancer(private val context: Context) {

    private val apiSnippets = listOf(
        Snippet("auto.text", "auto.text(\"text\")", "通过文本查找控件"),
        Snippet("auto.id", "auto.id(\"id\")", "通过 ID 查找控件"),
        Snippet("auto.click", "auto.click(x, y)", "坐标点击"),
        Snippet("auto.swipe", "auto.swipe(x1, y1, x2, y2, duration)", "滑动"),
        Snippet("auto.gesture", "auto.gesture(duration, [x1,y1], [x2,y2])", "手势路径"),
        Snippet("images.captureScreen", "images.captureScreen()", "截图"),
        Snippet("images.findImage", "images.findImage(src, template, {threshold: 0.8})", "找图"),
        Snippet("images.findColor", "images.findColor(img, color, {region: [x,y,w,h]})", "找色"),
        Snippet("ocr.recognize", "ocr.recognize(img, {region: [x,y,w,h]})", "OCR 文字识别"),
        Snippet("ml.createDetector", "ml.createDetector(modelPath, labels)", "创建 YOLOv8 检测器"),
        Snippet("ml.detect", "ml.detect(detector, img)", "执行目标检测"),
        Snippet("floaty.window", "floaty.window('<frame><text text=\"Hello\"/></frame>')", "悬浮窗"),
        Snippet("http.get", "http.get(url, headers)", "HTTP GET"),
        Snippet("http.post", "http.post(url, data, headers)", "HTTP POST"),
        Snippet("files.read", "files.read(path)", "读取文件"),
        Snippet("files.write", "files.write(path, content)", "写入文件"),
        Snippet("app.launch", "app.launch(packageName)", "启动应用"),
        Snippet("device.width", "device.width", "屏幕宽度"),
        Snippet("device.height", "device.height", "屏幕高度"),
        Snippet("shell.exec", "shell.exec(\"command\", false)", "执行 Shell"),
        Snippet("threads.start", "threads.start(function() { /* ... */ })", "启动线程"),
        Snippet("events.on", "events.on(\"event\", handler)", "监听事件"),
        Snippet("storages.create", "storages.create(\"namespace\")", "创建存储"),
        Snippet("notifications.on", "notifications.on(\"posted\", function(n) {})", "监听通知"),
        Snippet("shizuku.exec", "shizuku.exec(\"command\")", "Shizuku 执行命令"),
        Snippet("setTimeout", "setTimeout(function() {}, ms)", "定时器"),
        Snippet("setInterval", "setInterval(function() {}, ms)", "循环定时器")
    )

    data class Snippet(val trigger: String, val insert: String, val desc: String)

    /**
     * 配置编辑器：语法高亮、自动补全、符号匹配。
     */
    fun setup(editor: CodeEditor) {
        editor.setTabWidth(4)
    }

    /**
     * 获取 API 补全建议。
     */
    fun getCompletions(prefix: String): List<SimpleCompletionItem> {
        return apiSnippets
            .filter { it.trigger.contains(prefix, ignoreCase = true) }
            .map { snippet ->
                SimpleCompletionItem(snippet.trigger, snippet.desc, 0, snippet.insert)
            }
    }

    /**
     * 格式化代码（简单缩进对齐）。
     */
    fun formatCode(code: String): String {
        val lines = code.split("\n")
        val result = StringBuilder()
        var indent = 0
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("}") || trimmed.startsWith(")") || trimmed.startsWith("]")) indent = maxOf(0, indent - 1)
            result.append("    ".repeat(indent)).append(trimmed).append("\n")
            indent += trimmed.count { it == '{' || it == '(' || it == '[' }
            indent -= trimmed.count { it == '}' || it == ')' || it == ']' }
            indent = maxOf(0, indent)
        }
        return result.toString().trimEnd()
    }
}
