plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "org.autojs.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.autojs.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(Modules.CORE_RUNTIME))
    implementation(project(Modules.CORE_API))
    implementation(project(Modules.TOOLKIT_ACCESSIBILITY))
    implementation(project(Modules.TOOLKIT_VISION))
    implementation(project(Modules.TOOLKIT_FLOATY))
    implementation(project(Modules.TOOLKIT_NETWORK))
    implementation(project(Modules.TOOLKIT_STORAGE))
    implementation(project(Modules.TOOLKIT_DEVICE))
    implementation(project(Modules.TOOLKIT_SHELL))
    implementation(project(Modules.TOOLKIT_ML))
    implementation(project(Modules.TOOLKIT_NOTIFICATION))
    implementation(project(Modules.TOOLKIT_SHIZUKU))
    implementation(project(Modules.FEATURE_SCHEDULER))
    implementation(project(Modules.FEATURE_PACKAGER))
    implementation(project(Modules.FEATURE_INSPECTOR))
    implementation(project(Modules.FEATURE_DEVPLUGIN))
    implementation(project(Modules.FEATURE_TASKER))

    implementation(Deps.CORE_KTX)
    implementation(Deps.APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.CONSTRAINT_LAYOUT)
    implementation(Deps.RECYCLER_VIEW)
    implementation(Deps.LIFECYCLE_RUNTIME)
    implementation(Deps.LIFECYCLE_VIEWMODEL)
    implementation(Deps.COROUTINES)
    implementation(Deps.ROOM_RUNTIME)
    implementation(Deps.ROOM_KTX)
    ksp(Deps.ROOM_COMPILER)
    implementation(Deps.MMKV)
    implementation(Deps.GSON)
    implementation(Deps.WORK_MANAGER)
    implementation(Deps.SORA_EDITOR)

    debugImplementation(Deps.LEAKCANARY)
}
