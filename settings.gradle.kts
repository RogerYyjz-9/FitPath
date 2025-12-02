// File: settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // ✅ 统一版本：Kotlin 2.0.x + Compose plugin 同版本 + KSP 前缀同 Kotlin
    val kotlinVersion = "2.0.21"

    plugins {
        id("com.android.application") version "8.2.2"
        id("org.jetbrains.kotlin.android") version kotlinVersion

        // ✅ Kotlin 2.0+ 必须：Compose Compiler Gradle plugin
        id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion

        // ✅ 必须与 Kotlin 前缀一致
        id("com.google.devtools.ksp") version "${kotlinVersion}-1.0.25"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FitPath"
include(":app")
