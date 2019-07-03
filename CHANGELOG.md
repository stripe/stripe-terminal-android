# 1.0.0-b9

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:1.0.0-b9"
```

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

# 1.0.0-b7

## Added annotations to all public facing methods/params
Note that this will change the nullability of some parameters if you're writing a Kotlin app.

## Other changes
- Removed obsolete `InstallUpdateCallback`
