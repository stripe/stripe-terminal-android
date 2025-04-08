import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("kotlin-kapt")
}

android {
    val minSdkVersion: Int by project
    val latestSdkVersion: Int by project

    namespace = "com.stripe.example"
    compileSdk = latestSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
        targetSdk = latestSdkVersion

        val backendUrl = project.property("EXAMPLE_BACKEND_URL").toString().trim('"')
        buildConfigField("String", "EXAMPLE_BACKEND_URL", "\"$backendUrl\"")
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
}

// Force Kapt task to use Java 8. See https://youtrack.jetbrains.com/issue/KT-55947.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

val androidxLifecycleVersion = "2.6.2"
val kotlinCoroutinesVersion = "1.7.3"
val retrofitVersion = "2.11.0"
val stripeTerminalVersion = "4.3.1"

dependencies {
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycleVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutinesVersion")

    // OK HTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    // Stripe Terminal library
    implementation("com.stripe:stripeterminal-taptopay:$stripeTerminalVersion")
    implementation("com.stripe:stripeterminal-core:$stripeTerminalVersion")

    // Leak canary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}
