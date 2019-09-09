# 1.0.0-rc1

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:1.0.0-rc1"
```

## Switched to BLE for reader discovery
This should speed up discovery times slightly, and will prevent some edge cases where the user has
connected to the reader via their device's settings menu, since BLE devices aren't able to connect
from that menu.

## Made the SDK lifecycle aware
We've made the SDK lifecycle aware so that we can optimize resource use according to what stage of
life the app is in. This change will also give us more debugging info for any future issues.
NOTE: With this change, you'll need to register our lifecycle observer in a few of your
`Application` subclass' methods. See [the installation guide](https://github.com/stripe/stripe-terminal-android#installation)
for more information.

## Added a static `isInitialized` method to `Terminal`
This can be used to check if there's an existing Terminal object before calling `getInstance`.

## Other changes
- Made all user-facing objects Parcelable
- Reorganized SDK into packages, so you might need to update your imports

# 1.0.0-b9

## Added a number of new error codes
Added a bunch of new error codes that should make failures clearer. One to highlight is
`MUST_BE_DISCOVERING_TO_CONNECT`, which explicitly verifies that `discoverReaders` is running when
`connectReader` is called. If it isn't, this error is thrown to your `connectReader` callback. This
is to prevent unpredictable failures due to caching readers.

## Other changes
- Renamed `CONFIRM_PAYMENT_INTENT_ERROR` to `PAYMENT_DECLINED_BY_STRIPE_API` to stay consistent
- Fixed error with dip on cards with multiple applications

# 1.0.0-b8

## Multiple connectivity fixes
We made a few fixes that should prevent the reader from disconnecting as often, and should make
reconnection more consistent when it does.

## Other changes
- Fixed issue preventing some parameters in `PaymentIntentParameters` from doing anything
- Made `ErrorCallback` public to help with Xamarin integration
- Prevented race condition with BBPOS hardware from failing operations
