package org.autojs.toolkit.network

import org.autojs.core.api.ApiProvider
import org.autojs.core.api.ScriptScope

/**
 * http 命名空间 API：HTTP 请求。
 */
class HttpApiProvider : ApiProvider {
    override val name = "http"
    private val client = HttpClient()

    override fun install(scope: ScriptScope) {
        scope.defineObject("http") {
            function("get") { args ->
                val url = args[0]?.toString() ?: return@function null
                @Suppress("UNCHECKED_CAST")
                val headers = (args.getOrNull(1) as? Map<String, String>) ?: emptyMap()
                client.get(url, headers)
            }
            function("post") { args ->
                val url = args[0]?.toString() ?: return@function null
                val data = args[1]?.toString() ?: ""
                @Suppress("UNCHECKED_CAST")
                val headers = (args.getOrNull(2) as? Map<String, String>) ?: emptyMap()
                client.post(url, data, headers)
            }
            function("postJson") { args ->
                val url = args[0]?.toString() ?: return@function null
                val json = args[1]?.toString() ?: ""
                client.postJson(url, json)
            }
            function("put") { args ->
                val url = args[0]?.toString() ?: return@function null
                val data = args[1]?.toString() ?: ""
                client.put(url, data)
            }
            function("delete") { args ->
                val url = args[0]?.toString() ?: return@function null
                client.delete(url)
            }
            function("download") { args ->
                val url = args[0]?.toString() ?: return@function false
                val dest = args[1]?.toString() ?: return@function false
                client.download(url, dest)
            }
            function("uploadFile") { args ->
                val url = args[0]?.toString() ?: return@function null
                val path = args[1]?.toString() ?: return@function null
                client.uploadFile(url, path)
            }
            function("newWebSocket") { args ->
                val url = args[0]?.toString() ?: return@function null
                // WebSocket 需要特殊处理，返回连接对象
                null
            }
        }
    }
}
