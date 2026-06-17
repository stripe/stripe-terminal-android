plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.stripe.example.ktor3test"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.stripe.example.ktor3test"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

val stripeTerminalVersion = "5.6.0"

dependencies {
    // Stripe Terminal SDK
    implementation("com.stripe:stripeterminal-core:$stripeTerminalVersion")

    // Local lib compiled against Ktor 2.3.13 (simulates a third-party lib using Ktor 2)
    implementation(project(":ktor2lib"))

    // Ktor 3.x with BOM to align all ktor modules
    implementation(platform("io.ktor:ktor-bom:3.1.3"))
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
}
