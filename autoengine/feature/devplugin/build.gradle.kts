plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.autojs.feature.devplugin"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    api(project(Modules.CORE_RUNTIME))
    api(project(Modules.TOOLKIT_NETWORK))
    api(project(Modules.TOOLKIT_STORAGE))
    implementation(Deps.CORE_KTX)
    implementation(Deps.COROUTINES)
    implementation(Deps.OKHTTP)
    implementation(Deps.GSON)
}
