package org.autojs.toolkit.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP 客户端：封装 OkHttp，提供同步请求 API。
 * 脚本侧 http.get() / http.post() / http.download() 等。
 */
class HttpClient(
    timeoutMs: Long = 30000L
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .build()

    data class Response(
        val statusCode: Int,
        val body: String,
        val headers: Map<String, String>,
        val requestTime: Long
    )

    fun get(url: String, headers: Map<String, String> = emptyMap()): Response {
        return request("GET", url, headers, null)
    }

    fun post(url: String, data: String, headers: Map<String, String> = emptyMap()): Response {
        return request("POST", url, headers, data)
    }

    fun postJson(url: String, json: String, headers: Map<String, String> = emptyMap()): Response {
        val h = headers.toMutableMap(); h["Content-Type"] = "application/json"
        return request("POST", url, h, json)
    }

    fun put(url: String, data: String, headers: Map<String, String> = emptyMap()): Response {
        return request("PUT", url, headers, data)
    }

    fun delete(url: String, headers: Map<String, String> = emptyMap()): Response {
        return request("DELETE", url, headers, null)
    }

    fun request(method: String, url: String, headers: Map<String, String>, body: String?): Response {
        val builder = Request.Builder().url(url)
        headers.forEach { (k, v) -> builder.header(k, v) }
        when (method.uppercase()) {
            "GET" -> builder.get()
            "POST" -> builder.post(RequestBody.create("text/plain".toMediaType(), body ?: ""))
            "PUT" -> builder.put(RequestBody.create("text/plain".toMediaType(), body ?: ""))
            "DELETE" -> builder.delete()
        }
        val start = System.currentTimeMillis()
        client.newCall(builder.build()).execute().use { resp ->
            return Response(
                resp.code,
                resp.body?.string() ?: "",
                resp.headers.toMap(),
                System.currentTimeMillis() - start
            )
        }
    }

    fun download(url: String, destPath: String, headers: Map<String, String> = emptyMap()): Boolean {
        val builder = Request.Builder().url(url)
        headers.forEach { (k, v) -> builder.header(k, v) }
        return try {
            client.newCall(builder.build()).execute().use { resp ->
                if (!resp.isSuccessful) return false
                resp.body?.byteStream()?.use { input ->
                    File(destPath).outputStream().use { input.copyTo(it) }
                }
                true
            }
        } catch (e: IOException) { false }
    }

    fun uploadFile(url: String, filePath: String, fieldName: String = "file", headers: Map<String, String> = emptyMap()): Response {
        val file = File(filePath)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(fieldName, file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .build()
        val builder = Request.Builder().url(url).post(body)
        headers.forEach { (k, v) -> builder.header(k, v) }
        client.newCall(builder.build()).execute().use { resp ->
            return Response(resp.code, resp.body?.string() ?: "", resp.headers.toMap(), 0)
        }
    }

    fun newWebSocket(url: String, listener: WebSocketListener): WebSocket {
        return client.newWebSocket(Request.Builder().url(url).build(), listener)
    }

    fun close() = client.dispatcher.executorService.shutdown()
}
