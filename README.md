# Stripe Terminal Android <img src="https://img.shields.io/badge/Beta-yellow.svg">

# Requirements

The Stripe Terminal Android SDK is compatible with apps supporting Android API level 21 and above.

# Try the example app

The Stripe Terminal Android SDK includes an open-source example app, which you can use to familiarize yourself with the SDK before starting your own integration. To get started with the example app, clone the repo from \[Github\](https://github.com/stripe/stripe-terminal-android).

To build the example app:

1. Import the `Example` project into Android Studio
2. Navigate to our [example backend](https://github.com/stripe/example-terminal-backend) and click the button to deploy it on Heroku.
2. In `ApiClient.kt`, set the URL of the Heroku app you just deployed
3. Build and run the app. The app includes a reader simulator, so you have no need for a physical reader to start your integration. Note that while the example app will work in an Android emulator, you will only be able to connect to a simulated reader due to lack of bluetooth capabilities. 

## Installation
In order to use the Android version of the Terminal SDK, you first have to add the SDK to the `dependencies` block of your `build.gradle` file:


    dependencies {
      implementation "com.stripe:stripeterminal:1.0.0-b3"
    }
    
Next, since the SDK relies on Java 8, you’ll need to specify that as your target Java version (also in `build.gradle`:


    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

### Configure your app

Location access must be enabled in order to use the SDK. You’ll need to make sure that the `ACCESS_COARSE_LOCATION` permission is enabled in your app. To do this, add the following check before you initialize the `Terminal` object:


    if (ContextCompat.checkSelfPermission(getActivity(), 
      Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
        
        // REQUEST_CODE should be defined on your app level
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CODE);
    }

 You should also verify that the user allowed the location permission, since the SDK won’t function without it. To do this, override the `onRequestPermissionsResult` method in your app and check the permission result.


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("Location services are required in order to " +
                    "connect to a reader.");
        }
    }


> Note: Stripe needs to know where payments occur to reduce risks associated with those charges and to minimize disputes. If the SDK can’t determine the Android device’s location, payments are disabled until location access is restored.


## Documentation
 - [Getting Started](https://stripe.com/docs/terminal/sdk/android)
 - [API Reference](https://stripe.dev/stripe-terminal-android)


