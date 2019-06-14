# 1.0.0-b8

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:1.0.0-b8"
```

## Multiple connectivity fixes
We made a few fixes that should prevent the reader from disconnecting as often, and should make
reconnection more consistent when it does.

## Other changes
- Fixed issue preventing some parameters in `PaymentIntentParameters` from doing anything
- Made `ErrorCallback` public to help with Xamarin integration
- Prevented race condition with BBPOS hardware from failing operations

# 1.0.0-b7

## Added annotations to all public facing methods/params
Note that this will change the nullability of some parameters if you're writing a Kotlin app.

## Other changes
- Removed obsolete `InstallUpdateCallback`

# 1.0.0-b5

## Disconnect reader when session is invalidated
Added a `SESSION_EXPIRED` error code that will be thrown when the Stripe session is out-of-date.
This should be handled by prompting the user to reconnect to any device.

## Added ALREADY_CONNECTED_TO_READER error code
In order to prevent the user from getting into a bad state by connecting to multiple readers, we'll
now return this error code on `discoverReaders` or `connectReader` when already connected to a
reader.

## Other changes
- Fix NullPointerException on some unexpected disconnects
- Update example app to handle unexpected disconnects smoothly
