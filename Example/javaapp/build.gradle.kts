plugins {
    id("com.android.application")
}

android {
    val minSdkVersion: Int by project
    val latestSdkVersion: Int by project

    namespace = "com.stripe.example.javaapp"
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

    lint {
        enable += "Interoperability"
        disable += "MergeRootFrame"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

val androidxLifecycleVersion = "2.6.2"
val retrofitVersion = "2.11.0"
val stripeTerminalVersion = "4.3.1"

dependencies {
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Annotations
    implementation("org.jetbrains:annotations:24.1.0")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-livedata:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel:$androidxLifecycleVersion")

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
