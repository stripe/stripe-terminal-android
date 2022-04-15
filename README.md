# Stripe Terminal Android

For information on migrating from earlier versions of the Android SDK, see the [migration guide](https://stripe.com/docs/terminal/sdk-migration-guide).

# Requirements

The Stripe Terminal Android SDK is compatible with apps supporting Android API level 21* and above. Apps can be written using Kotlin or [Java 8](https://developer.android.com/studio/write/java8-support).

\* Note that attempting to override `minSdkVersion` to decrease the minimum supported API level will not work due to internal runtime API level validation.

# Try the example app

The Stripe Terminal Android SDK includes two open-source example apps (one in Java and the other in Kotlin), which you can use to familiarize yourself with the SDK before starting your own integration. To build the example app:

1. Clone this repo.
2. Import the `Example` project into Android Studio.
3. Navigate to our [example backend](https://github.com/stripe/example-terminal-backend) and click the button to deploy it on Heroku.
4. In `ApiClient.kt` (or `ApiClient.java` if you're using the Java example), set the URL of the Heroku app you just deployed.
5. Build and run the app. The app includes a reader simulator, so you have no need for a physical reader to start your integration. Note that while the example app will work in an Android emulator, you will only be able to connect to a simulated reader due to lack of Bluetooth capabilities.

## Installation

To use the Android SDK, add the SDK to the `dependencies` block of your `build.gradle` file:


    dependencies {
        implementation "com.stripe:stripeterminal:2.8.1"
    }
    
Next, since the SDK relies on Java 8, you’ll need to specify that as your target Java version (also in `build.gradle`):


    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

Then, ensure that your kotlin version is >= 1.5.0 (in your application-level `build.gradle`):


    buildscript {
        repositories {
            ...
        }
        dependencies {
            classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0"
            ...
        }
    }

And in your module-specific `build.gradle`


    dependencies {
        implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.0"
    }

### Configure your app

Location access must be enabled in order to use the SDK. You’ll need to make sure that the `ACCESS_COARSE_LOCATION` permission is enabled in your app. To do this, add the following check before you initialize the `Terminal` object:

```java
if (ContextCompat.checkSelfPermission(getActivity(), 
  Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
        
    // REQUEST_CODE should be defined on your app level
    ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CODE_LOCATION);
}
```

 You should also verify that the user allowed the location permission, since the SDK won’t function without it. To do this, override the `onRequestPermissionsResult` method in your app and check the permission result.

```java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0
            && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        throw new RuntimeException("Location services are required in order to " +
                "connect to a reader.");
    }
}
```


**Note**: Stripe needs to know where payments occur to reduce risks associated with those charges and to minimize disputes. If the SDK can’t determine the Android device’s location, payments are disabled until location access is restored.

### Have an Application Class

The Android SDK is lifecycle aware. To prevent memory leaks and ensure proper cleanup of long-running Terminal SDK processes, your application must have the `Application` subclass where `TerminalApplicationDelegate` is used to inform the SDK of lifecycle events.

This subclass should do the following:

* Call `TerminalApplicationDelegate.onCreate` from your application's `onCreate` method.
* Implement the [onTrimMemory](https://developer.android.com/reference/android/app/Application#onTrimMemory(int)) method and call `TerminalApplicationDelegate.onTrimMemory` from your implementation.

For example:

```kotlin
// Substitute with your application name, and remember to keep it the same as your AndroidManifest.xml
class StripeTerminalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TerminalApplicationDelegate.onCreate(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        TerminalApplicationDelegate.onTrimMemory(this, level)
    }
}
```

Lastly, don't forget to set your Application class in your `AndroidManifest.xml` accordingly. See the following taken from the example app:

```xml
<application
    android:name=".StripeTerminalApplication" // Or whatever your application class name is
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

## Documentation
 - [Getting Started](https://stripe.com/docs/terminal/sdk/android)
 - [2.x API Reference](https://stripe.dev/stripe-terminal-android)
 - [1.x API Reference](https://stripe.dev/stripe-terminal-android/v1)
