object Versions {
    const val CORE_KTX = "1.13.1"
    const val APPCOMPAT = "1.7.0"
    const val MATERIAL = "1.12.0"
    const val CONSTRAINT_LAYOUT = "2.1.4"
    const val RECYCLER_VIEW = "1.3.2"
    const val LIFECYCLE = "2.8.6"
    const val COROUTINES = "1.9.0"
    const val OKHTTP = "4.12.0"
    const val ROOM = "2.6.1"
    const val MMKV = "1.3.11"
    const val RHINO = "1.7.15"
    const val OPENCV = "4.10.0"
    const val MLKIT_TEXT = "16.0.1"
    const val SORA_EDITOR = "0.20.4"
    const val WORK_MANAGER = "2.9.1"
    const val LEAKCANARY = "2.14"
    const val GSON = "2.11.0"
    const val JUNIT = "4.13.2"
    const val MOCKK = "1.13.13"
    const val ESPRESSO = "3.6.1"
    const val TURNO = "1.0.0"
    const val ONNX_RUNTIME = "1.26.0"
    const val SHIZUKU = "13.1.5"
    const val XUTILS = "3.9.0"
}

object Deps {
    const val CORE_KTX = "androidx.core:core-ktx:${Versions.CORE_KTX}"
    const val APPCOMPAT = "androidx.appcompat:appcompat:${Versions.APPCOMPAT}"
    const val MATERIAL = "com.google.android.material:material:${Versions.MATERIAL}"
    const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.CONSTRAINT_LAYOUT}"
    const val RECYCLER_VIEW = "androidx.recyclerview:recyclerview:${Versions.RECYCLER_VIEW}"

    const val LIFECYCLE_RUNTIME = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}"
    const val LIFECYCLE_SERVICE = "androidx.lifecycle:lifecycle-service:${Versions.LIFECYCLE}"
    const val LIFECYCLE_VIEWMODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}"

    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}"

    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"
    const val OKHTTP_LOGGING = "com.squareup.okhttp3:logging-interceptor:${Versions.OKHTTP}"

    const val ROOM_RUNTIME = "androidx.room:room-runtime:${Versions.ROOM}"
    const val ROOM_KTX = "androidx.room:room-ktx:${Versions.ROOM}"
    const val ROOM_COMPILER = "androidx.room:room-compiler:${Versions.ROOM}"

    const val MMKV = "com.tencent:mmkv:${Versions.MMKV}"
    const val RHINO = "org.mozilla:rhino:${Versions.RHINO}"
    const val GSON = "com.google.code.gson:gson:${Versions.GSON}"

    const val WORK_MANAGER = "androidx.work:work-runtime-ktx:${Versions.WORK_MANAGER}"

    const val LEAKCANARY = "com.squareup.leakcanary:leakcanary-android:${Versions.LEAKCANARY}"

    const val MLKIT_TEXT = "com.google.mlkit:text-recognition-chinese:${Versions.MLKIT_TEXT}"

    const val ONNX_RUNTIME = "com.microsoft.onnxruntime:onnxruntime-android:${Versions.ONNX_RUNTIME}"
    const val SHIZUKU_API = "dev.rikka.shizuku:api:${Versions.SHIZUKU}"
    const val SHIZUKU_PROVIDER = "dev.rikka.shizuku:provider:${Versions.SHIZUKU}"
    const val SORA_EDITOR = "io.github.Rosemoe.sora-editor:editor:${Versions.SORA_EDITOR}"

    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val MOCKK = "io.mockk:mockk:${Versions.MOCKK}"
    const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}"
}

object Modules {
    const val CORE_API = ":core:api"
    const val CORE_RUNTIME = ":core:runtime"
    const val TOOLKIT_ACCESSIBILITY = ":toolkit:accessibility"
    const val TOOLKIT_VISION = ":toolkit:vision"
    const val TOOLKIT_FLOATY = ":toolkit:floaty"
    const val TOOLKIT_NETWORK = ":toolkit:network"
    const val TOOLKIT_STORAGE = ":toolkit:storage"
    const val TOOLKIT_DEVICE = ":toolkit:device"
    const val TOOLKIT_SHELL = ":toolkit:shell"
    const val FEATURE_SCHEDULER = ":feature:scheduler"
    const val FEATURE_PACKAGER = ":feature:packager"
    const val TOOLKIT_ML = ":toolkit:ml"
    const val TOOLKIT_NOTIFICATION = ":toolkit:notification"
    const val TOOLKIT_SHIZUKU = ":toolkit:shizuku"
    const val FEATURE_INSPECTOR = ":feature:inspector"
    const val FEATURE_DEVPLUGIN = ":feature:devplugin"
    const val FEATURE_TASKER = ":feature:tasker"
}
