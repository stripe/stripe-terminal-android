plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.stripe.example.ktor2lib"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Ktor 2.3.13 — matches what the Terminal SDK compiles against
    implementation("io.ktor:ktor-client-core:2.3.13")
    implementation("io.ktor:ktor-client-okhttp:2.3.13")
}
