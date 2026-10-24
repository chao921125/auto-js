package org.autojs.feature.devplugin

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * DevPlugin 服务端：通过 WebSocket 实现 VS Code 插件联动。
 *
 * 替代旧版 app/pluginclient/ 下的 DevPluginService/Router/Handler，
 * 使用原生 WebSocket 协议（RFC 6455）替代旧版 Java WebSocket 库。
 *
 * 协议兼容 Auto.js VS Code Extension：
 * - 设备连接后发送 hello 消息
 * - VS Code 可下发命令：run_script / save_script / list_files
 * - 设备上报事件：log / error / script_finished
 */
class DevPluginServer(private val port: Int = 9317) {

    data class DeviceInfo(
        val name: String,
        val id: String,
        val version: String,
        val type: String = "android"
    )

    interface DevPluginCallback {
        fun onConnected(client: String)
        fun onDisconnected(client: String)
        fun onCommand(cmd: String, data: JsonObject): JsonObject?
    }

    private val gson = Gson()
    private val callbacks = mutableListOf<DevPluginCallback>()
    private val clients = ConcurrentHashMap<String, ClientConnection>()
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var deviceInfo: DeviceInfo? = null

    fun setDeviceInfo(info: DeviceInfo) { deviceInfo = info }
    fun addCallback(cb: DevPluginCallback) { callbacks.add(cb) }
    fun removeCallback(cb: DevPluginCallback) { callbacks.remove(cb) }

    fun start() {
        if (isRunning) return
        isRunning = true
        Thread {
            try {
                serverSocket = ServerSocket(port)
                while (isRunning) {
                    val socket = serverSocket?.accept() ?: break
                    handleClient(socket)
                }
            } catch (e: Exception) {
                if (isRunning) e.printStackTrace()
            }
        }.start()
    }

    fun stop() {
        isRunning = false
        clients.values.forEach { it.close() }
        clients.clear()
        serverSocket?.close()
        serverSocket = null
    }

    private fun handleClient(socket: Socket) {
        val clientId = socket.remoteSocketAddress.toString()
        try {
            val input = socket.getInputStream()
            val output = socket.getOutputStream()
            performHandshake(input, output)
            val conn = ClientConnection(clientId, output)
            clients[clientId] = conn
            callbacks.forEach { it.onConnected(clientId) }
            sendHello(conn)
            readMessages(clientId, input, conn)
        } catch (e: Exception) {
            clients.remove(clientId)
            callbacks.forEach { it.onDisconnected(clientId) }
        }
    }

    private fun performHandshake(input: InputStream, output: OutputStream) {
        val reader = java.io.BufferedReader(java.io.InputStreamReader(input))
        val headers = mutableMapOf<String, String>()
        var line = reader.readLine()
        while (line.isNotEmpty()) {
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) headers[parts[0].trim().lowercase()] = parts[1].trim()
            line = reader.readLine()
        }
        val key = headers["sec-websocket-key"] ?: return
        val acceptKey = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-1")
                .digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteArray())
        )
        val response = "HTTP/1.1 101 Switching Protocols\r\n" +
            "Upgrade: websocket\r\n" +
            "Connection: Upgrade\r\n" +
            "Sec-WebSocket-Accept: $acceptKey\r\n\r\n"
        output.write(response.toByteArray())
        output.flush()
    }

    private fun sendHello(conn: ClientConnection) {
        val info = deviceInfo ?: return
        val hello = JsonObject().apply {
            addProperty("type", "hello")
            add("data", gson.toJsonTree(mapOf(
                "device_name" to info.name,
                "device_id" to info.id,
                "version" to info.version,
                "type" to info.type
            )))
        }
        conn.send(hello.toString())
    }

    private fun readMessages(clientId: String, input: InputStream, conn: ClientConnection) {
        val buffer = ByteArray(8192)
        while (isRunning && clients.containsKey(clientId)) {
            val len = input.read(buffer)
            if (len <= 0) break
            if ((buffer[0].toInt() and 0xFF) and 0x0F == 0x1) {
                val payloadLen = buffer[1].toInt() and 0x7F
                val payloadStart = if (payloadLen <= 125) 2 else 4
                val actualLen = payloadLen.coerceAtMost(len - payloadStart)
                if (actualLen > 0) {
                    val payload = String(buffer, payloadStart, actualLen)
                    handleMessage(clientId, payload, conn)
                }
            }
        }
        clients.remove(clientId)
        callbacks.forEach { it.onDisconnected(clientId) }
    }

    private fun handleMessage(clientId: String, message: String, conn: ClientConnection) {
        try {
            val json = JsonParser.parseString(message).asJsonObject
            val type = json.get("type")?.asString ?: return
            val data = json.getAsJsonObject("data") ?: JsonObject()
            val result = callbacks.mapNotNull { it.onCommand(type, data) }.firstOrNull()
            if (result != null) {
                val response = JsonObject().apply {
                    addProperty("type", "response")
                    add("data", result)
                }
                conn.send(response.toString())
            }
        } catch (e: Exception) {
            val error = JsonObject().apply {
                addProperty("type", "error")
                addProperty("message", e.message ?: "unknown error")
            }
            conn.send(error.toString())
        }
    }

    fun broadcastLog(message: String, level: String = "info") {
        val log = JsonObject().apply {
            addProperty("type", "log")
            add("data", gson.toJsonTree(mapOf("message" to message, "level" to level)))
        }
        clients.values.forEach { it.send(log.toString()) }
    }

    fun broadcastScriptFinished(scriptId: String, success: Boolean) {
        val event = JsonObject().apply {
            addProperty("type", "script_finished")
            add("data", gson.toJsonTree(mapOf("id" to scriptId, "success" to success)))
        }
        clients.values.forEach { it.send(event.toString()) }
    }

    fun getConnectedClients(): Set<String> = clients.keys.toSet()
    fun isRunning(): Boolean = isRunning

    private class ClientConnection(val id: String, private val output: OutputStream) {
        fun send(text: String) {
            val data = text.toByteArray()
            synchronized(output) {
                if (data.size <= 125) {
                    output.write(byteArrayOf(0x81.toByte(), data.size.toByte()))
                } else {
                    output.write(byteArrayOf(0x81.toByte(), 126.toByte(),
                        (data.size shr 8).toByte(), data.size.toByte()))
                }
                output.write(data)
                output.flush()
            }
        }

        fun close() {
            try { output.close() } catch (e: Exception) { }
        }
    }
}
