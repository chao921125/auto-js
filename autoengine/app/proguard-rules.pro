# Rhino
-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# MMKV
-keep class com.tencent.mmkv.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# AutoJs API
-keep class org.autojs.core.api.** { *; }
-keep class org.autojs.toolkit.** { *; }
-keep class org.autojs.feature.** { *; }
-keep class org.autojs.core.runtime.** { *; }

# Keep annotations
-keepattributes RuntimeVisibleAnnotations
-keepattributes Signature
-keepattributes *Annotation*
