1.0.0-b4

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:1.0.0-b4"
```

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

1.0.0-b3

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:1.0.0-b3"
```

## Allows example app use in Android emulator
Made the changes necessary to allow the example app to be used by an Android emulator.
NOTE: This will only work with a simulated reader, since there are no bluetooth capabilities
available in Android emulators.

## Other changes

- Fixed bug preventing metadata passed in `PaymentIntentParameters` from showing up on the
PaymentIntent.
- The `ReadReusableCardParameters` object passed to `Terminal#readReusableCard` now includes a
`customer` parameter which, if included, will attach the newly created `PaymentMethod` to the
specified customer.
