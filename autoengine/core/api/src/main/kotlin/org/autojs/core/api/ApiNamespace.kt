package org.autojs.core.api

/**
 * 标注一个 Kotlin 单例/对象为脚本 API 命名空间。
 * 引擎启动时通过反射扫描此注解，将对象方法注册为 JS 全局函数。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiNamespace(
    val name: String,
    val description: String = ""
)
