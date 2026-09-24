pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        // The SDK 6.0.0 release candidate is downloaded with `kts terminal-sdk download-rc`.
        mavenLocal {
            content {
                includeModuleByRegex("com\\.stripe", "stripeterminal.*")
            }
        }
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.3.21" apply false
}

include(":javaapp")
include(":kotlinapp")
