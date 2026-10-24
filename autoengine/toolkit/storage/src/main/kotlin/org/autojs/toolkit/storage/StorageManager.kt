package org.autojs.toolkit.storage

import com.tencent.mmkv.MMKV

/**
 * 存储管理器：基于 MMKV 的键值持久化，按脚本命名空间隔离。
 * 脚本侧 storages.create("name").put("key", value).get("key")
 */
class StorageManager {

    private val storages = mutableMapOf<String, ScriptStorage>()

    fun create(name: String): ScriptStorage {
        return storages.getOrPut(name) { ScriptStorage(MMKV.mmkvWithID(name)) }
    }

    fun remove(name: String) {
        storages.remove(name)
        MMKV.mmkvWithID(name).clearMemoryCache()
    }

    fun clearAll() {
        storages.values.forEach { it.clear() }
        storages.clear()
    }
}

/**
 * 脚本存储：单脚本命名空间的键值操作。
 */
class ScriptStorage(private val mmkv: MMKV) {

    fun put(key: String, value: Any?): Boolean {
        when (value) {
            is String -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Long -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is ByteArray -> mmkv.encode(key, value)
            null -> mmkv.removeValueForKey(key)
            else -> mmkv.encode(key, value.toString())
        }
        return true
    }

    fun get(key: String, default: Any? = null): Any? {
        return when (default) {
            is String -> mmkv.decodeString(key, default)
            is Int -> mmkv.decodeInt(key, default)
            is Long -> mmkv.decodeLong(key, default)
            is Float -> mmkv.decodeFloat(key, default)
            is Double -> mmkv.decodeDouble(key, default)
            is Boolean -> mmkv.decodeBool(key, default)
            is ByteArray -> mmkv.decodeBytes(key)
            else -> mmkv.decodeString(key, null)
        }
    }

    fun contains(key: String): Boolean = mmkv.contains(key)

    fun remove(key: String) = mmkv.removeValueForKey(key)

    fun clear() = mmkv.clearAll()

    fun keys(): Array<String> = mmkv.allKeys()?.map { it ?: "" }?.toTypedArray() ?: emptyArray()
}
