# Android Terminal SDK Installation Doc

# Requirements

The Stripe Terminal Android SDK is compatible with apps supporting Android API level 24 and above.

# Try the example app

The Stripe Terminal SDK includes an example Android app, which you can use to familiarize yourself with the SDK before starting your own integration. To get started with the example app, clone the repo from \[Github\](https://github.com/stripe/stripe-terminal-android). In this repository, you’ll also find API reference documentation for the SDK.

To build the example app:

1. Import the `Example` project into Android Studio
2. In `BackendSimulator.java`, set your Stripe Account’s test secret API key. A backend simulator has been included as part of the example app for demonstration purposes only. Note that you should *never* store your Stripe account's secret API key in your own app. For more information, see [https://stripe.com/docs/keys](https://stripe.com/docs/keys)
3. Build and run the app. Note that the example app won’t work on a simulated device due to lack of Bluetooth connectivity. In the future, the SDK will include a reader simulator, so you can get started without any physical hardware.


# Getting started
## Step 1: Set up the SDK

In order to use the Android version of the Terminal SDK, you’ll first have to add the SDK to the `dependencies` block of your `build.gradle` file:


    dependencies {
      implementation "com.stripe.stripeterminal:a0.1"
    }

Additionally, since the SDK doesn’t include transitive dependencies, you’ll have to include a few of its dependencies as well:


    dependencies {
      implementation "com.jakewharton.timber:timber:4.7.1"
      implementation "com.google.dagger:dagger:2.17"
      implementation "com.squareup.okhttp3:okhttp:3.11.0"
      implementation "com.google.guava:guava:26.0-android
    }

Next, since the SDK relies on Java 8, you’ll need to specify that as your target Java version (also in `build.gradle`:


    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

**Configure your app**

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


## Step 2: Set up the connection token endpoint

To connect to a reader, the Android SDK needs to retrieve a short-lived connection token from Stripe, proxied through your server. On your backend, add an endpoint that creates a connection token and returns its value. 

Refer to our \[iOS documentation\](https://stripe.com/docs/terminal/ios#connection-token) for instructions on setting up this endpoint.

To give the SDK access to this endpoint, implement the `ConnectionTokenProvider` interface in your app, which defines a single function that requests a connection token from your backend.


    public class MyTokenProvider implements ConnectionTokenProvider {
      @Override
      public void fetchConnectionToken(ConnectionTokenCallback callback) {
        try {
          // Your backend should call v1/terminal/connection_tokens and return the 
          // JSON response from Stripe. When the request to your backend succeeds, 
          // return the `secret` from the response to the SDK.
          callback.onSuccess(secret);
        } catch (Exception e) {
          callback.onFailure(
            new ConnectionTokenException("Failed to fetch connection token", e));
        }
      }
    }

This function is called whenever the SDK is initialized. It's also called when a new token is needed to connect to a reader (for example, when your app disconnects from a reader). If the SDK is unable to retrieve a new token from your backend, connecting to a reader fails with the error from your server.


> Do not cache or hardcode the connection token. The iOS SDK manages the token's lifecycle.
## Step 3: Create your Terminal instance

The `Terminal` object made available by the Stripe Terminal SDK exposes a generic interface for discovering readers, connecting to a reader, and creating payments. To initialize a `Terminal` instance, you’ll need to provide your ConnectionTokenProvider implemented in Step 2. The listener you provide can be used to handle events from the SDK, such as disconnects.


    // Create your listener object. Override any methods that you want to be notified of.
    TerminalListener listener = new TerminalListener() {};
    // Create your token provider.
    MyTokenProvider tokenProvider = new MyTokenProvider();
    // Pass in your API key, the listener you created, and the current application context
    Terminal terminal = Terminal.initTerminal(getActivity(), tokenProvider, listener);


## Step 4: Connect to a reader

After setting up your reader, call `terminal.discoverReaders` in your application to find and display readers to connect to. You will need to provide a `DiscoveryListener` to handle updating your app as the SDK updates the list of discovered readers.


    DiscoveryConfiguration config = new DiscoveryConfiguration();
    terminal.discoverReaders(config, readers -> {
      System.out.println(readers);
    }, new DiscoveryCallback() {
      @Override
      public void onSuccess() {
        System.out.println("Finished discovering readers");
      }
      
      @Override
      public void onFailure(TerminalException e) {
        e.printStackTrace();
      }
    }

When discovering readers, your app can either display an updating list of discovered readers, or automatically select a reader (e.g. if you are discovering using bluetooth proximity). To connect to a reader, call `connectReader`.



# Collecting payments
## Step 1: Create a PaymentIntent

First, create a [PaymentIntent](https://stripe.com/docs/api#payment_intents) object. A PaymentIntent represents your intent to collect a payment from a customer, tracking the lifecycle of the payment process through each step

Each PaymentIntent typically correlates with a single cart or customer session in your application. When you create a PaymentIntent, specify the currency and the amount to collect from the customer.

The following example shows how to create a PaymentIntent [client-side](https://stripe.github.io/stripe-terminal-ios/docs/Classes/SCPPaymentIntentParameters.html) via the SDK:


    PaymentIntentParameters params = new PaymentIntentParameters.Builder()
      .setAmount(100)
      .setCurrency("usd")
      .build();
    terminal.createPaymentIntent(params, new PaymentIntentCallback() {
      // Placeholder for continueing payment with the created PaymentIntent
    }

You can choose to cancel stale, uncaptured PaymentIntents via the SDK or [on your server](https://stripe.com/docs/api#cancel_payment_intent). Canceling a PaymentIntent releases all uncaptured funds. A canceled PaymentIntent can no longer be used to perform charges.

If the information required to create a PaymentIntent isn’t readily available in your application, you can also create the PaymentIntent [on your server](https://stripe.com/docs/terminal/ios/workflows#server-payment-intent), and retrieve it via the SDK.


> Stripe Terminal is currently available only in the 50 United States. It currently supports transactions only within the same geographic boundaries, and only in USD.


## Step 2: Confirm the PaymentIntent

After creating a PaymentIntent, its status will be `requires_source`. Use the `terminal.collectPaymentMethod` to collect a payment method with the PaymentIntent. The SDK then waits for a payment method to be presented to the reader.

First, implement the `ReaderInputListener` interface to handle events from the reader while it accepts input. Your app should surface these events to the user.


    public class MyReaderListener implements ReaderInputListener {
      @Override
      public void onBeginWaitingForReaderInput(ReaderInputOptions options) {
        System.out.println("Reader requests input in one of the following methods: " + 
          options.toString());
      }
      
      @Override
      public void onRequestReaderInputPrompt(ReaderInputPrompt prompt) {
        System.out.println("Reader prompts for the following action: " + 
          prompt.toString());
      }
    }

Now you can pass the listener into `collectPaymentMethod` in order to read a card:


    // This should be the payment intent you received back from createPaymentIntent
    PaymentIntent paymentIntent; 
    Cancelable cancelable = terminal.collectPaymentMethod(paymentIntent, 
      new MyReaderListener(),
      new PaymentIntentCallback() {
        @Override
        public void onSuccess(PaymentIntent intent) {
          // Placeholder for confirming the updated PaymentIntent
        }
      
        @Override
        public void onFailure(TerminalException e) {
          // Placeholder for handling the error
        }
      });

If collecting a payment method fails, the callback completes with an exception. If collecting a payment method succeeds, the callback completes with the updated PaymentIntent, which will now have a status of `requires_confirmation`.

To proceed with the payment, call `terminal.confirmPaymentIntent`.


    // This should be the PaymentIntent you received back from collectPaymentMethod
    PaymentIntent paymentIntent;
    Cancelable cancelable = terminal.confirmPaymentIntent(paymentIntent, 
      new PaymentIntentCallback() {
        @Override
        public void onSuccess(PaymentIntent intent) {
          // Placeholder for notifying your backend to capture the PaymentIntent
        }
      
        @Override
        public void onFailure(TerminalException e) {
          // Placeholder for handling the error
        }
      });

If confirming the PaymentIntent fails, the callback completes with an exception. If confirming the PaymentIntent succeeds, the completion block completes with the updated PaymentIntent, which will now have a status of `requires_capture`.


> PaymentIntents confirmed through the Terminal SDKs are uncaptured. Make sure your backend is set up to capture the PaymentIntent, as explained in Step 4, within 24 hours. Otherwise, the authorization expires and the funds are released back to the customer.


## Step 3: Handle failures

When confirming a `PaymentIntent` fails, the SDK returns an error that includes the updated `PaymentIntent`. Your application should inspect the `PaymentIntent` to decide how to deal with the error.

If the updated `PaymentIntent`’s status is `REQUIRES_SOURCE` (e.g., the request failed because the payment method was declined), call `collectPaymentMethod` with the updated `PaymentIntent` to try charging another payment method.

If the updated PaymentIntent’s status is `REQUIRES_CONFIRMATION` (e.g., the request failed because of a temporary connectivity problem), call `confirmPaymentIntent` again with the updated PaymentIntent to retry the request.

If the updated PaymentIntent is `null`, the request to Stripe’s servers timed out, and the `PaymentIntent`’s status is unknown. In this situation, retry confirming the original `PaymentIntent`. Do not create a new `PaymentIntent`, as that could result in multiple authorizations for the cardholder.

If you encounter multiple, consecutive timeouts, there might be local networking problems. In this case, make sure that your application is able to communicate with the Internet.


> You might want to reconcile PaymentIntents with your internal orders system to clean up unresolved authorizations. For example, if a PaymentIntent is stale, and is unassociated with an order on your server, you can [cancel](https://stripe.com/docs/api#cancel_payment_intent) it. Canceled PaymentIntents can’t be used for payments.


## Step 4: Capture the PaymentIntent

Stripe Terminal uses a two-step process to prevent unintended and duplicate payments. When the SDK returns a confirmed `PaymentIntent` to your app, the payment is authorized, but not captured. Read our [Auth and capture](https://stripe.com/docs/charges#auth-and-capture) documentation for more information about the difference.

When your app receives a confirmed PaymentIntent from the SDK, it should notify your backend to capture the PaymentIntent. You should create an endpoint on your backend that accepts a PaymentIntent ID and sends a request to the Stripe API to capture it.

Refer to our [iOS documentation](https://stripe.com/docs/terminal/ios/payment#capture) for instructions on capturing a PaymentIntent on your backend.


