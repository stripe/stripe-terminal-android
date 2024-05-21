// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

if (!project.hasProperty("EXAMPLE_BACKEND_URL")) {
    error("You must specify EXAMPLE_BACKEND_URL in gradle.properties")
}

ext {
    set("minSdkVersion", 26)
    set("latestSdkVersion", 34)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(project.layout.buildDirectory)
}
