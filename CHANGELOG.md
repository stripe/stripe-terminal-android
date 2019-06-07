# 1.0.0-b7

## Added annotations to all public facing methods/params
Note that this will change the type of some parameters if you're writing a Kotlin app.

## Other changes
- Removed obsolete `InstallUpdateCallback`

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:1.0.0-b7"
```

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

# 1.0.0-b4

## Returning generated card ID after `processPayment`
After a successful payment, a generated card ID will be included on the `CardPresentDetails` object.
This can be used for recurring payments with the same card. Note: This ID will only be returned for
swiped/inserted payments (i.e. not contactless).

## ReaderSoftwareUpdateCallback called with null when no update available
Instead of calling the `onFailure` method with exception type `NO_AVAILABLE_READER_SOFTWARE_UPDATE`,
we'll now call `onSuccess` with `null`.

## Other changes

- Stop prompting for tap during `readReusableCard`
- Check for location services on SDK initialization to avoid failing at payment creation
- Added overloaded `initTerminal` method with no need for `LogLevel` parameter (defaults to NONE)

