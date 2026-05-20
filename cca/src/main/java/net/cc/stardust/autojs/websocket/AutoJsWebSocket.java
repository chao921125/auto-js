package net.cc.stardust.autojs.websocket;

import android.os.Handler;
import android.os.Looper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket客户端封装
 * Auto.js Pro新特性: 新增WebSocket模块
 * 
 * 功能:
 * - 双向实时通信
 * - 支持ws/wss协议
 * - 自动重连机制
 * - 消息缓冲区的处理
 * - 事件驱动模型
 */
public class AutoJsWebSocket {
    
    private static final int DEFAULT_RECONNECT_DELAY = 3000; // 3秒
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    
    private String url;
    private WebSocketClient client;
    private Handler mainHandler;
    
    // 事件监听器
    private Map<String, Object> eventListeners;
    
    // 配置选项
    private WebSocketConfig config;
    
    // 消息队列
    private ConcurrentHashMap<String, Object> messageQueue;
    
    public AutoJsWebSocket(String url) {
        this(url, new WebSocketConfig());
    }
    
    public AutoJsWebSocket(String url, WebSocketConfig config) {
        this.url = url;
        this.config = config;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.eventListeners = new ConcurrentHashMap<>();
        this.messageQueue = new ConcurrentHashMap<>();
        
        initialize();
    }
    
    /**
     * 初始化WebSocket连接
     */
    private void initialize() {
        try {
            WebSocketClient client = createWebSocketClient();
            client.connect();
            this.client = client;
            
            emit("connected", new HashMap<String, Object>() {{
                put("url", url);
                put("timestamp", System.currentTimeMillis());
            }});
            
        } catch (Exception e) {
            emit("error", new HashMap<String, Object>() {{
                put("message", e.getMessage());
                put("type", "connection");
            }});
        }
    }
    
    /**
     * 创建WebSocket客户端实例
     */
    private WebSocketClient createWebSocketClient() {
        return new WebSocketClient(java.net.URI.create(url), getDraft()) {
            
            @Override
            public void onOpen(ServerHandshake handshake) {
                mainHandler.post(() -> {
                    emit("open", new HashMap<String, Object>() {{
                        put("handshake", "N/A (headers not available)");
                        put("reconnected", false);
                    }});
                });
            }
            
            @Override
            public void onMessage(String message) {
                mainHandler.post(() -> {
                    emit("message", new HashMap<String, Object>() {{
                        put("data", message);
                        put("timestamp", System.currentTimeMillis());
                    }});
                    
                    // 处理特定类型消息
                    handleMessageType(message);
                });
            }
            
            // @Override - commented out for compatibility
            public void onMessage(byte[] message) {
                mainHandler.post(() -> {
                    emit("binary", new HashMap<String, Object>() {{
                        put("data", message);
                        put("timestamp", System.currentTimeMillis());
                    }});
                });
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                mainHandler.post(() -> {
                    emit("close", new HashMap<String, Object>() {{
                        put("code", code);
                        put("reason", reason);
                        put("remote", remote);
                    }});
                    
                    // 尝试重连
                    if (remote && config.isAutoReconnect()) {
                        attemptReconnect(reason);
                    }
                });
            }
            
            @Override
            public void onError(Exception ex) {
                mainHandler.post(() -> {
                    emit("error", new HashMap<String, Object>() {{
                        put("message", ex.getMessage());
                        put("type", "websocket");
                    }});
                    ex.printStackTrace();
                });
            }
        };
    }
    
    /**
     * 获取WebSocket协议版本
     */
    private Draft getDraft() {
        Draft draft = new Draft_6455(new DefaultExtension());
        // 可以添加自定义header
        return draft;
    }
    
    /**
     * 发送文本消息
     */
    public void send(String data) {
        if (client != null && client.isOpen()) {
            client.send(data);
            emit("sent", new HashMap<String, Object>() {{
                put("data", data);
                put("timestamp", System.currentTimeMillis());
            }});
        } else {
            // 消息入队
            messageQueue.put(String.valueOf(System.currentTimeMillis()), data);
            emit("queued", new HashMap<String, Object>() {{
                put("data", data);
            }});
        }
    }
    
    /**
     * 发送二进制消息
     */
    public void send(byte[] data) {
        if (client != null && client.isOpen()) {
            client.send(data);
        }
    }
    
    /**
     * 关闭连接
     */
    public void close() {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }
    
    /**
     * 关闭连接(带代码和原因)
     */
    public void close(int code, String reason) {
        if (client != null) {
            client.close(code, reason);
        }
    }
    
    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return client != null && client.isOpen();
    }
    
    /**
     * 注册事件监听器
     */
    public void on(String event, WebSocketEventListener listener) {
        eventListeners.put(event, listener);
    }
    
    /**
     * 移除事件监听器
     */
    public void off(String event) {
        eventListeners.remove(event);
    }
    
    /**
     * 触发事件
     */
    private void emit(String event, Object data) {
        Object listener = eventListeners.get(event);
        if (listener instanceof WebSocketEventListener) {
            ((WebSocketEventListener) listener).onEvent(data);
        }
    }
    
    /**
     * 处理特定类型的消息
     */
    private void handleMessageType(String message) {
        // 可以根据消息类型分发到不同的处理器
        // 例如: JSON-RPC、自定义协议等
    }
    
    /**
     * 尝试重连
     */
    private int reconnectAttempts = 0;
    
    private void attemptReconnect(String reason) {
        if (reconnectAttempts >= config.getMaxReconnectAttempts()) {
            emit("reconnect_failed", new HashMap<String, Object>() {{
                put("attempts", reconnectAttempts);
                put("reason", reason);
            }});
            return;
        }
        
        reconnectAttempts++;
        long delay = config.getReconnectDelay() * reconnectAttempts; // 递增延迟
        
        mainHandler.postDelayed(() -> {
            try {
                client.reconnect();
                reconnectAttempts = 0; // 重置计数器
                
                // 重连后发送队列中的消息
                flushMessageQueue();
                
            } catch (Exception e) {
                attemptReconnect("reconnect_failed");
            }
        }, delay);
    }
    
    /**
     * 刷新消息队列
     */
    private void flushMessageQueue() {
        for (String key : messageQueue.keySet()) {
            String message = (String) messageQueue.get(key);
            if (message != null) {
                send(message);
            }
        }
        messageQueue.clear();
    }
    
    /**
     * WebSocket事件监听器接口
     */
    public interface WebSocketEventListener {
        void onEvent(Object data);
    }
    
    /**
     * WebSocket配置类
     */
    public static class WebSocketConfig {
        private int reconnectDelay;           // 重连延迟(毫秒)
        private int maxReconnectAttempts;     // 最大重连次数
        private boolean autoReconnect;        // 是否自动重连
        private Proxy proxy;                  // 代理设置
        
        public WebSocketConfig() {
            this.reconnectDelay = DEFAULT_RECONNECT_DELAY;
            this.maxReconnectAttempts = MAX_RECONNECT_ATTEMPTS;
            this.autoReconnect = true;
            this.proxy = null;
        }
        
        public int getReconnectDelay() {
            return reconnectDelay;
        }
        
        public WebSocketConfig setReconnectDelay(int reconnectDelay) {
            this.reconnectDelay = reconnectDelay;
            return this;
        }
        
        public int getMaxReconnectAttempts() {
            return maxReconnectAttempts;
        }
        
        public WebSocketConfig setMaxReconnectAttempts(int maxReconnectAttempts) {
            this.maxReconnectAttempts = maxReconnectAttempts;
            return this;
        }
        
        public boolean isAutoReconnect() {
            return autoReconnect;
        }
        
        public WebSocketConfig setAutoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }
        
        public Proxy getProxy() {
            return proxy;
        }
        
        public WebSocketConfig setProxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }
    }
}
