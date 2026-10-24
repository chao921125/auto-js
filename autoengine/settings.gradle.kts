pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(uri("/Users/huangchao/Work/GitHub/auto-js/local-maven"))
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "autoengine"

include(":app")
include(":core:api")
include(":core:runtime")
include(":toolkit:accessibility")
include(":toolkit:vision")
include(":toolkit:floaty")
include(":toolkit:network")
include(":toolkit:storage")
include(":toolkit:device")
include(":toolkit:shell")
include(":toolkit:ml")
include(":toolkit:notification")
include(":toolkit:shizuku")
include(":feature:scheduler")
include(":feature:packager")
include(":feature:inspector")
include(":feature:devplugin")
include(":feature:tasker")
