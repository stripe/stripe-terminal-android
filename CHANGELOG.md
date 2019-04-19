1.0.0-b2

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:1.0.0-b2"
```

## Added the ability to update reader software
The update reader flow consists of two methods. The first method (`checkForUpdate`) will return a
`ReaderSoftwareUpdate` object if an update is available, or throw a
`NO_AVAILABLE_READER_SOFTWARE_UPDATE` exception if it isn't. The second method (`installUpdate`)
will handle installation if necessary.

## Made ApiError field public on TerminalException
If a `TerminalException` is of type `API_ERROR`, it will contain an `ApiError` field giving more
details about exactly what went wrong.

## Added CARD_READ_TIMED_OUT error code
Now, if the reader times out (which will happen 60 seconds after `collectPaymentMethod` is called),
a `CARD_READ_TIMED_OUT` error will be thrown.

## Added PaymentMethodDetails to the PaymentIntent object
A `PaymentMethodDetails` object has been added to the `PaymentIntent`. This will surface details
about the payment method used to complete the `PaymentIntent`, like it's brand, expiration date,
and country.

## Implemented readReusableCard method
The `readReusableCard` method can be used to read a card and save it as a `PaymentMethod` to be used
in the future (e.g. for subscriptions).
NOTE: Most integrations should not use `readReusableCard`. If your integration only needs to read
cards and charge them immediately, you should instead be using `Terminal#collectPaymentMethod` and
`Terminal#processPayment`.

## Added a setTerminalListener method to Terminal
Now you can change the `TerminalListener` on the fly in order to continue receiving updates in a
new activity. Use the `setTerminalListener` method to do this.

## Made onUnexpectedReaderDisconnect mandatory
While the rest of the functions on the `TerminalListener` interface are optional, we think the
`onUnexpectedReaderDisconnect` method is important enough to be implemented in all integrations.

## Renamed confirmPaymentIntent -> processPayment

This `Terminal` method was renamed to be clearer about the purpose of the method, regardless of the
implementation details.

## Renamed ReaderInputListener -> ReaderDisplayListener

The `ReaderInputListener` was renamed to `ReaderDisplayListener` to be clearer about its purpose.
Additionally, both of its methods were renamed (from `onBeginWaitingForReaderInput` and
`onRequestReaderInputPrompt` to `onRequestReaderInput` and `onRequestDisplayReaderMessage`). The
functionality has remained the same.

