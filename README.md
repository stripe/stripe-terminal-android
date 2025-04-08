# Stripe Terminal Android

For information on migrating from earlier versions of the Android SDK, see the [migration guide](https://stripe.com/docs/terminal/sdk-migration-guide).

# Requirements

The Stripe Terminal Android SDK is compatible with apps supporting Android API level 26* and above. Apps can be written using Kotlin or [Java 8](https://developer.android.com/studio/write/java8-support).

Note that attempting to override `minSdkVersion` to decrease the minimum supported API level will not work due to internal runtime API level validation.

# Try the example app

The Stripe Terminal Android SDK includes two open-source example apps (one in Java and the other in Kotlin), which you can use to familiarize yourself with the SDK before starting your own integration. To build the example app:

1. Clone this repo.
2. Import the `Example` project into Android Studio.
3. Navigate to our [example backend](https://github.com/stripe/example-terminal-backend) and deploy it following the instructions in the README.
4. In `gradle.properties`, set the URL of the backend you just deployed.
5. Build and run the app. The app includes a reader simulator, so you have no need for a physical reader to start your integration. Note that while the example app will work in an Android emulator, you will only be able to connect to a simulated reader due to lack of Bluetooth capabilities.

## Installation

To use the Android SDK, add `stripeterminal` to the `dependencies` block of your `build.gradle.kts` file:

```kotlin
dependencies {
  implementation("com.stripe:stripeterminal:4.3.1")
}
```

### Configure your app

Location access must be enabled in order to use the SDK. You’ll need to make sure that the `ACCESS_FINE_LOCATION` permission is enabled in your app. To do this, add the following check before you initialize the `Terminal` object:

```kotlin
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    // Define the REQUEST_CODE_LOCATION on your app level
    ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION)
}
```

You should also verify that the user allowed the location permission, since the SDK won’t function without it. To do this, override the `onRequestPermissionsResult` method in your app and check the permission result:

```kotlin
override fun onRequestPermissionsResult(
  requestCode: Int,
  permissions: Array<String>,
  grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE_LOCATION && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) { 
        throw RuntimeException("Location services are required in order to connect to a reader.") 
    }
}
```

**Note**: Stripe needs to know where payments occur to reduce risks associated with those charges and to minimize disputes. If the SDK can’t determine the Android device’s location, payments are disabled until location access is restored.

### Have an Application Class

The Android SDK is lifecycle aware. To prevent memory leaks and ensure proper cleanup of long-running Terminal SDK processes, your application must have the `Application` subclass where `TerminalApplicationDelegate` is used to inform the SDK of lifecycle events.

This subclass should call `TerminalApplicationDelegate.onCreate` from your application's `onCreate` method. For example:

```kotlin
// Substitute with your application name, and remember to keep it the same as your AndroidManifest.xml
class StripeTerminalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TerminalApplicationDelegate.onCreate(this)
    }
}
```

Lastly, don't forget to set your Application class in your `AndroidManifest.xml` accordingly. See the following taken from the example app:

```xml
<!-- Replace the android:name value with your application class name -->
<application
    android:name=".StripeTerminalApplication"
    android:allowBackup="false"
    android:icon="@mipmap/launcher"
    android:label="@string/app_name"
    android:supportsRtl="true"
    android:theme="@style/Theme.Example"
    tools:ignore="GoogleAppIndexingWarning">
    <activity android:name="com.stripe.example.MainActivity"
        android:screenOrientation="fullSensor">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />

            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

# Tap to Pay on Android (TTPA)

To use the Tap to Pay SDK, replace your existing `stripeterminal` dependencies in the `dependencies` block of
your `build.gradle.kts` file with the following:

```kotlin
dependencies {
  implementation("com.stripe:stripeterminal-taptopay:4.3.1")
  implementation("com.stripe:stripeterminal-core:4.3.1")
}
```

Please note that:
- You must use an SDK version greater than the minimum SDK version documented in our [user docs](https://stripe.com/docs/terminal/payments/setup-reader/tap-to-pay?platform=android)
- The `stripeterminal-taptopay` SDK version _must_ match the version you're using for other `stripeterminal` libraries.
- There are no public APIs provided by this SDK. Please reference Stripe Terminal's public API references for more information on how to use the SDK.

## Documentation
 - [Getting Started](https://stripe.com/docs/terminal/sdk/android)
 - [4.x API Reference](https://stripe.dev/stripe-terminal-android)
 - [3.x API Reference](https://stripe.dev/stripe-terminal-android/v3)
 - [2.x API Reference](https://stripe.dev/stripe-terminal-android/v2)
 - [1.x API Reference](https://stripe.dev/stripe-terminal-android/v1)
 - [Support Lifecycle & Release Status](SUPPORT.md)
