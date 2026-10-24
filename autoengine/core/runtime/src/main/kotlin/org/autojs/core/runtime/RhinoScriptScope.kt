package org.autojs.core.runtime

import org.autojs.core.api.ScriptScope
import org.autojs.core.api.ScriptObjectBuilder
import org.mozilla.javascript.*

/**
 * Rhino ScriptScope 适配器：将 Rhino 的 ScriptableObject 适配为 ScriptScope 接口。
 */
class RhinoScriptScope(
    private val cx: Context,
    private val parentScope: ScriptableObject,
    private val target: ScriptableObject
) : ScriptScope {

    override fun defineFunction(name: String, fn: (Array<out Any?>) -> Any?) {
        val f = object : BaseFunction() {
            override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array<out Any?>): Any? {
                return fn(args)
            }
        }
        ScriptableObject.defineProperty(target, name, f, ScriptableObject.EMPTY)
    }

    override fun defineObject(name: String, builder: ScriptObjectBuilder.() -> Unit) {
        val obj = cx.newObject(parentScope) as ScriptableObject
        val b = ScriptObjectBuilder().apply(builder)
        installBuilderMembers(obj, b.build())
        ScriptableObject.defineProperty(target, name, obj, ScriptableObject.EMPTY)
    }

    override fun defineProperty(name: String, value: Any?) {
        val jsValue = Context.javaToJS(value, target)
        ScriptableObject.defineProperty(target, name, jsValue, ScriptableObject.EMPTY)
    }

    override fun get(name: String): Any? {
        return target.get(name, target)
    }

    private fun installBuilderMembers(obj: ScriptableObject, members: Map<String, Any?>) {
        for ((key, value) in members) {
            when (value) {
                is Function<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val fn = value as (Array<out Any?>) -> Any?
                    val f = object : BaseFunction() {
                        override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array<out Any?>): Any? {
                            return fn(args)
                        }
                    }
                    ScriptableObject.defineProperty(obj, key, f, ScriptableObject.EMPTY)
                }
                is ScriptObjectBuilder -> {
                    val nested = cx.newObject(parentScope) as ScriptableObject
                    installBuilderMembers(nested, value.build())
                    ScriptableObject.defineProperty(obj, key, nested, ScriptableObject.EMPTY)
                }
                else -> {
                    val jsValue = Context.javaToJS(value, obj)
                    ScriptableObject.defineProperty(obj, key, jsValue, ScriptableObject.EMPTY)
                }
            }
        }
    }
}
