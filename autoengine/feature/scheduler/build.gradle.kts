plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.autojs.feature.scheduler"
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
    implementation(Deps.CORE_KTX)
    implementation(Deps.WORK_MANAGER)
    implementation(Deps.COROUTINES)
    implementation(Deps.GSON)
}
