package org.autojs.core.api

import java.util.concurrent.ConcurrentHashMap

/**
 * API 注册中心：管理所有脚本可用的命名空间（auto, images, floaty, http, files, ...）。
 * 引擎启动时调用 install() 将所有已注册的 API 注入 JS 作用域。
 */
object ApiRegistry {

    private val namespaces = ConcurrentHashMap<String, ApiProvider>()

    fun register(provider: ApiProvider) {
        namespaces[provider.name] = provider
    }

    fun unregister(name: String) {
        namespaces.remove(name)
    }

    fun get(name: String): ApiProvider? = namespaces[name]

    fun all(): Collection<ApiProvider> = namespaces.values

    fun clear() = namespaces.clear()
}

/**
 * API 提供者接口：每个命名空间（auto/images/http/...）实现此接口，
 * 在 install 时将自身方法注入到 JS scope 中。
 */
interface ApiProvider {
    val name: String
    fun install(scope: ScriptScope)
}

/**
 * 脚本作用域抽象：隔离不同 JS 引擎（Rhino / QuickJS）的 scope 差异。
 */
interface ScriptScope {
    fun defineFunction(name: String, fn: (Array<out Any?>) -> Any?)
    fun defineObject(name: String, builder: ScriptObjectBuilder.() -> Unit)
    fun defineProperty(name: String, value: Any?)
    fun get(name: String): Any?
}

/**
 * 脚本对象构建器：用于在 JS scope 中创建嵌套对象（如 auto.text, auto.click）。
 */
class ScriptObjectBuilder {
    private val members = mutableMapOf<String, Any?>()

    fun function(name: String, fn: (Array<out Any?>) -> Any?) {
        members[name] = fn
    }

    fun property(name: String, value: Any?) {
        members[name] = value
    }

    fun nested(name: String, builder: ScriptObjectBuilder.() -> Unit) {
        members[name] = ScriptObjectBuilder().apply(builder)
    }

    fun build(): Map<String, Any?> = members
}
