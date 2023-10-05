# CHANGELOG

This document details changes made to the SDK by version. The current status
of each release can be found in the [Support Lifecycle](SUPPORT.md).

## 3.0.0 - 2023-09-08

3.0.0 includes breaking changes in both APIs and behavior. See the [migration guide](https://stripe.com/docs/terminal/references/sdk-migration-guide?terminal-sdk-platform=android) for more details.

### Core
- Update: The [`PaymentIntent::id`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-payment-intent/id.html) is now nullable to support creating Payment Intents while offline. This feature is in an invite-only beta. See [Collect payments while offline](https://stripe.com/docs/terminal/features/operate-offline/collect-payments) for details.
- Update: The `minSdkVersion` has been updated to 26. This means that the SDK will no longer support devices running Android 7.1.2 (Nougat) or earlier. Older devices can continue to use the 2.x versions of the SDK while on the maintenance schedule.
- Update: The Android-specific `Parcelable` interface has been replaced with the more Java-generic `Serializable` for all external models.
- Update: [`DiscoveryConfiguration`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-discovery-configuration/index.html) has been converted to a sealed type, instead of relying on the `DiscoveryMethod` enum to disambiguate different discovery methods.
- Update: Runtime permission checks have been moved from [`Terminal.initTerminal()`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/-companion/init-terminal.html) to [`Terminal.discoverReaders()`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/discover-readers.html).
  - Bluetooth permissions are now only required when discovering readers via [`BluetoothDiscoveryConfiguration`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-discovery-configuration/-bluetooth-discovery-configuration/index.html).
  - Location permissions will continue to be required for all [`DiscoveryConfigurations`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-discovery-configuration/index.html). Location services will also need to be enabled on the device at the time of discovery.
- Update: `Terminal.readReusableCard` has been removed. This functionality is replaced by [Setup Intents](https://stripe.com/docs/terminal/features/saving-cards/save-cards-directly?terminal-sdk-platform=android).
- Update: `Terminal.processPayment` has been renamed to [`Terminal.confirmPaymentIntent`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/confirm-payment-intent.html).
- Update: `Terminal.processRefund` has been renamed to [`Terminal.confirmRefund`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/confirm-refund.html).
- Update: [`Terminal.collectPaymentMethod`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/collect-payment-method.html) now takes an optional non-null [`CollectConfiguration`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-collect-configuration/index.html) parameter.
- Update: [`Terminal.collectSetupIntentPaymentMethod`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/collect-setup-intent-payment-method.html) now takes an optional non-null [`SetupIntentConfiguration`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-setup-intent-configuration/index.html) parameter.
- Update: For readers that require updates to be installed upon connecting, [`TerminalListener.onConnectionStatusChange()`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.callable/-terminal-listener/on-connection-status-change.html) will now be called with [`CONNECTED`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-connection-status/-c-o-n-n-e-c-t-e-d/index.html) _after_ the updates complete successfully, not before.
- Update: [`TerminalListener.onUnexpectedReaderDisconnect()`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.callable/-terminal-listener/on-unexpected-reader-disconnect.html) will be invoked if a command cannot be sent to an internet reader. Previously, this callback was only invoked when a periodic status check failed.
- Update: Deprecated classes and members have been replaced or removed:
  - `CaptureMethod.getManual()` has been removed. Use [`CaptureMethod.MANUAL`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-capture-method/-companion/-manual.html) instead.
  - The `CollectConfiguration` constructor has been removed. Use [`CollectConfiguration.Builder`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-collect-configuration/-builder/index.html) instead.
  - [`CollectConfiguration.moto`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-collect-configuration/moto.html) is no longer mutable.
  - `ConnectConfiguration.registerToLocation` has been removed and replaced with [`ConnectConfiguration.locationId`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-connection-configuration/location-id.html).
  - The `locationId` parameter from the [`HandoffConnectionConfiguration`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-connection-configuration/-handoff-connection-configuration/-handoff-connection-configuration.html) constructor has been removed.
  - `BluetoothReaderListener` and `UsbReaderListener` have been removed and replaced with [`ReaderListener`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.callable/-reader-listener/index.html).
  - `EmvBlob` has been marked as an internal class.
  - `Reader.device` has been removed and replaced with [`Reader.bluetoothDevice`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-reader/bluetooth-device.html) and [`Reader.usbDevice`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-reader/usb-device.html).
  - `Reader.registeredLocation` has been removed and replaced with [`Reader.location`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-reader/location.html).
  - `TerminalApplicationDelegate.onTrimMemory()` has been removed. It is automatically managed by the SDK.
  - `CardDetails.fingerprint` and `CardPresentDetails.fingerprint` have been removed from mobile SDKs. You will still be able to access the fingerprint server-side.

## 2.23.2 - 2023-10-05

### Handoff
- Fix: [CollectConfiguration.updatePaymentIntent](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-collect-configuration/update-payment-intent.html) now works as expected for handoff mode

## 2.23.1 - 2023-08-25

### Core
- Fix: Internal SDK fixes for error reporting and version reporting.

## 2.23.0 - 2023-08-07

### Core
- Update: SDKs have been updated to depend on [Kotlin 1.8.22](https://github.com/JetBrains/kotlin/releases/tag/v1.8.22).
- Update: Discovery of Bluetooth devices will now fail if Bluetooth is not enabled or available on the device.

## 2.22.0 - 2023-07-07

### Core
- New: [USB connectivity](https://stripe.com/docs/terminal/payments/connect-reader?terminal-sdk-platform=android&reader-type=usb)
  is now generally available via [`Terminal.connectUsbReader`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/connect-usb-reader.html)
  for the Stripe Reader M2, BBPOS WisePad 3, and BBPOS Chipper 2X readers.
- Update: `@UsbConnectivity` annotation is no longer required for using USB reader connection features.

## 2.21.1 - 2023-06-12

### Tap to Pay (localmobile)
- Fix: Don't call `TerminalStatusManager::unexpectedDisconnect` when the reader is disconnected intentionally

## 2.21.0 - 2023-06-05

### Core
- Update: SDKs have been updated to depend on [Kotlin 1.8.21](https://kotlinlang.org/docs/whatsnew1821.html).
- Update: `Terminal.retrievePaymentIntent` and `Terminal.retrieveSetupIntent` will no longer be blocking if an existing reader operation is in progress.
- New: Added Simulated Visa US Common Debit test card type: `VISA_US_COMMON_DEBIT`

### Tap to Pay (localmobile)
- Update: Using the production version of the localmobile reader with debuggable applications now fails during discovery
  with a `TerminalErrorCode::LOCAL_MOBILE_DEBUG_NOT_SUPPORTED` error for security and compliance reasons. Developers should
  test and integrate the Tap to Pay on Android SDK with a simulated version of the reader by setting
  `DiscoveryConfiguration.isSimulated` to true.
- Update: Move country validation to the backend to allow for more flexible country support.
- Update: Improve successful tap rate with Google Pay mobile wallets.
- Update: Add device-specific UX support for devices released in the last 6 months and popular Xiaomi devices.
- New: Add localized text to UX and error messages returned from the backend based on device locale.

## 2.20.1 - 2023-05-15

### Tap to Pay (localmobile)
- Fix: Fix an issue causing local declines with Visa/EFTPOS co-branded debit cards.

## 2.20.0 - 2023-05-10

### Core
- Fix: Errors from canceling and creating `PaymentIntents` and `SetupIntents` no longer return a generic "Unexpected null" message on internet readers.
- Fix: `CardDetails` now contains additional fields in [`GeneratedFrom`](https://stripe.com/docs/api/errors#errors-payment_method-card-generated_from) that describe the original `PaymentMethod`.
- Fix: Contactless payments on a BBPOS WisePad 3 taken with `CollectConfiguration.updatePaymentIntent` set to true will no longer take a few minutes to be processed.
- Fix: Reader updates will no longer fail when being performed on devices running Android 7 and earlier.
- New: Added `ReaderDisplayMessage.CARD_REMOVED_TOO_EARLY` sent when a card is removed too early during a contact payment.
- Update: A callback to `TerminalListener::onConnectionStatusChange` will be triggered when connecting to Internet readers, with `ConnectionState.CONNECTING`.

### Tap to Pay (localmobile)
- New: Add support for canceling the transaction via the Cancelable returned from `Terminal.collectPaymentMethod`.
- Fix: Fix `LOCAL_MOBILE_DEVICE_TAMPERED` connection failure after an application with backup enabled is uninstalled and reinstalled.
- Update: Enforce device compatibility checks on simulated localmobile reader.

## 2.19.0 - 2023-04-03

### Core
- New: Added `autoReconnectOnUnexpectedDisconnect` & `usbReaderReconnectionListener` to the [`UsbConnectionConfiguration`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-connection-configuration/-usb-connection-configuration/index.html). When enabled, the SDK will attempt to restore connection upon any unexpected disconnect to a USB reader. See [Stripe Docs](https://stripe.com/docs/terminal/payments/connect-reader?terminal-sdk-platform=android&reader-type=usb#automatic-reconnection) for details.
- New: Added support for simulating an on-reader tip for simulated readers that support on-reader tipping.
- New: Cancel `PaymentIntent` and `SetupIntent` via the SDK when connected to an internet reader by calling [`cancelPaymentIntent`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/cancel-payment-intent.html) or [`cancelSetupIntent`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/cancel-setup-intent.html) instead of using your backend.
    - _Note: This feature requires version `2.11.0.24` or later to be installed on your internet reader._
- New: When discovering simulated internet readers, a simulated WisePOS E reader is returned in the results.
- New: Added Simulated Co-branded Eftpos card types: `EFTPOS_AU_VISA_DEBIT` and `EFTPOS_AU_DEBIT_MASTERCARD`
- Update: Deprecated [`BluetoothReaderReconnectionListener`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.callable/-bluetooth-reader-reconnection-listener/index.html) and replaced with [`ReaderReconnectionListener`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.callable/-reader-reconnection-listener/index.html).
- Update: SDKs have been updated to depend on [Kotlin 1.8.10](https://kotlinlang.org/docs/whatsnew18.html).
- Fix: On-reader tips with BBPOS WisePad 3 now returns 0 [Amount](https://stripe.com/docs/api/payment_intents/object#payment_intent_object-amount_details-tip) when no tip is selected.
- Fix: Fixed a regression with creating a `PaymentIntent` in handoff mode when running on a device with an older version of the reader app.

### Tap to Pay (localmobile)
- Fix: Fix an issue for some American Express cards to improve client-side acceptance rates.
- New: Add beta support for accepting transactions in France, Ireland, and Germany.
- Update: Add new [TLS certificates](https://stripe.com/docs/tls-certificates) used by SDK with Stripe
  backend for certificate pinning.

## 2.18.1 - 2023-03-15

### Core
- Fix: `requestedPriority` is no longer dropped when performing a contactless tipping transaction with the WisePad 3.

### Tap to Pay (localmobile)
- Fix: Fix a crash that occurs when accepting MasterCard payments in minified applications:
  > java.lang.NoSuchMethodError: No virtual method getCurrency()Lcom/neovisionaries/i18n/CurrencyCode
- New: Add `Terminal.supportsReadersOfType` support to allow runtime checking of device hardware compatibility.
- New: Add simulator support. Use by setting `DiscoveryConfiguration.isSimulated` to true during reader discovery.
- Update: Increase minimum required Android OS version from 9 to 10 to comply with requirements to only
  allow collecting payments on currently supported versions of Android OS.
- Update: Reduce time taken to discover the Tap to Pay on Android reader by ~75%.

## 2.18.0 - 2023-03-06

### Core
- New: Create `PaymentIntent` and `SetupIntent` via the SDK when connected to an internet reader by calling [`createPaymentIntent`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/create-payment-intent.html) or [`createSetupIntent`](https://stripe.dev/stripe-terminal-android/core/com.stripe.stripeterminal/-terminal/create-setup-intent.html) instead of using your backend.
    - _Note: This feature requires version `2.11.0.0` or later to be installed on your internet reader._

## 2.17.1 - 2023-02-06

### Core
- Update: Move internal implementation classes to be private, so they are not visible to users. No functional changes.

### Tap to Pay (localmobile)
- Update: Improve `TerminalException.errorMessage` returned by the localmobile reader.
- Update: Significantly improve UX loading time.
- Fix: Fix gray screen that would display for up to a few seconds before UX appeared.
- Update: Refresh UX and add device-specific UX support for many devices.
- Update: Add new [TLS certificates](https://stripe.com/docs/tls-certificates) used by SDK with Stripe
  backend for certificate pinning.

## 2.17.0 - 2023-01-30

### Core
- New: `CardPresentDetails.incrementalAuthorizationStatus` indicates whether incremental authorizations are supported or not after the `PaymentIntent` has been confirmed.
- New: Statement descriptor suffix field added to `PaymentIntentParameters`, `PaymentIntent`, and `Charge`.
- New: Calculated statement descriptor field added to `Charge`.
- Update: Removed `Terminal.connectEmbeddedReader` method. It's meant only to be used internally by Stripe.
- Update: Internal refactor of our transaction state machine to increase observability for all readers.
- Fix: Don't log error when failing to retrieve serial number on API 29 and up. Fixes [issue 266](https://github.com/stripe/stripe-terminal-android/issues/266)
- Fix: Connecting to a reader via USB no longer starts a second application process.
- Fix: Fixed an issue where we weren't always resuming polling for mPOS battery status and location changes when the app is brought into the foreground.
- Fix: Don't crash when cancelling an intent after losing connection with a smart reader. Fixes [issue 275](https://github.com/stripe/stripe-terminal-android/issues/275)
- Fix: Fixed a bug with handling `captureMethod` and `setupFutureUsage` when creating a `PaymentIntent` in handoff integration mode.

### Tap to Pay (localmobile)
- Update: The `stripeterminal-localmobile` artifact will no longer automatically opt in to allow cleartext network connections to `localhost`.

## 2.16.0 - 2022-11-21
- New: Added support for creating Payment Intents with `CardPresentCaptureMethod.ManualPreferred` capture method set on the `CardPresentParameters`.
- Fix: Fixed an issue where cancelling discovery for USB readers may have left discovery broken until app restart.
- Update: Internal refactor of our transaction state machine to increase observability for Chipper 2X and Stripe M2 readers.

## 2.15.0 - 2022-10-31
- Fix: Corrected an incorrect property signature for `CaptureMethod.Manual` in java applications.
- Fix: Example apps now properly cancel payment intents created for smart readers. Requires a [backend change](https://github.com/stripe/example-terminal-backend/pull/42).
- Update: You can see the tip amount through the [`PaymentIntent`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-payment-intent/index.html) object in the [processPayment](https://stripe.dev/stripe-terminal-android/core/modules/core/com.stripe.stripeterminal/-terminal/process-payment.html) callback. Fixes [issue 228](https://github.com/stripe/stripe-terminal-android/issues/228)
- New: Added a `TippingConfiguration` object to be used to specify a custom amount for percentage based tip calculations. See [Tip-eligible amounts](https://stripe.com/docs/terminal/features/collecting-tips/on-reader#tip-eligible) for details.

## 2.14.0 - 2022-10-03
- Fix: `collectPaymentMethod` can now be called more than once, with or without an explicit cancel call. Fixes [issue 241](https://github.com/stripe/stripe-terminal-android/issues/241)

## 2.13.1 - 2022-09-21
- Fix: Corrected an issue causing incorrect currency displays on readers and causing Interac tap payments to be redirected to insert card. Fixes [issue 259](https://github.com/stripe/stripe-terminal-android/issues/259)

## 2.13.0 - 2022-08-29
**Note: This version contains an issue with currency displays and contactless Interac payments. Please use or migrate to 2.13.1 as soon as possible**

- Fix: Collecting a payment method now returns before card is removed for all reader types.
- New: Added a `PaymentIntentStatus` to represent when a `PaymentIntent` has been created and is being processed. Fixes [issue 236](https://github.com/stripe/stripe-terminal-android/issues/236)
- New: `ReaderInputOptions` now contains a list of currently available input options. Fixes [issue 248](https://github.com/stripe/stripe-terminal-android/issues/248).
- New: Trim quotes and spaces out of URL arguments in example app. Closes [PR 252](https://github.com/stripe/stripe-terminal-android/pull/252).

## 2.12.0 - 2022-07-25

- Update: @OnReaderTips annotation is no longer required for using the on-reader tipping feature
- New: Added `autoReconnectOnUnexpectedDisconnect` & `bluetoothReaderReconnectionListener` to the
  [`BluetoothConnectionConfiguration`](https://stripe.dev/stripe-terminal-android/external/com.stripe.stripeterminal.external.models/-connection-configuration/-bluetooth-connection-configuration/index.html).
  When enabled, the SDK will attempt to restore connection upon any unexpected disconnect to a reader. See [Stripe Doc](https://stripe.com/docs/terminal/payments/connect-reader?terminal-sdk-platform=android&reader-type=bluetooth#automatic-reconnection) for details.
- Fix: Fixed a `java.security.ProviderException` with message "Keystore operation failed" when calling `Terminal.initTerminal` for some Android devices.

## 2.11.0 - 2022-06-27

- Beta: Add ability to specify capture method when creating a PaymentIntent via the SDK.

## 2.10.2 - 2022-06-15

- Fix: Fixed a crash for devices running Android 7.1 and lower. See [issue 234](https://github.com/stripe/stripe-terminal-android/issues/234) for details.

## 2.10.0 - 2022-05-23

- New: Added currency characters to WisePad 3 display. See [issue 147](https://github.com/stripe/stripe-terminal-android/issues/147) for details.
- New: Refunds can now be collected when using a simulated reader. See [issue 226](https://github.com/stripe/stripe-terminal-android/issues/226) for details.
- Update: When connecting to Internet Readers, the SDK uses an embedded DNS to resolve reader IP
  addresses. This resolves [an error](https://support.stripe.com/questions/the-stripe-terminal-sdk-is-encountering-dns-errors-when-connecting-to-an-internet-reader) experienced by users of some DNS providers.
- Fix: Resolved `USB_PERMISSION_DENIED` error after granting permission. See [issue 231](https://github.com/stripe/stripe-terminal-android/issues/231) for details.

## 2.9.0 - 2022-04-25

- New: `onBatteryLevelUpdate` callback in `ReaderListener` is now triggered when connected to a simulated Bluetooth or USB reader.
- Fix: Resolved app crashes caused by `com.google.crypto.tink`. See [issue 222](https://github.com/stripe/stripe-terminal-android/issues/222) for details.

## 2.8.1 - 2022-04-15

- Fix: Reset cached tip amount before collecting payment to fix an issue with on-reader tipping beta for WP3 readers. Note that WPE readers are not affected by this bug.
  This issue manifests if a payment is collected with tipping enabled and subsequently a payment is collected without tipping enabled
  while the POS app is still alive. The payment without tipping enabled would use the cached tip amount.
  See [issue 224](https://github.com/stripe/stripe-terminal-android/issues/224)

## 2.8.0 - 2022-03-28

- Beta: Incremental or extended authorization can be requested with `CardPresentParameters` and
  `PaymentMethodOptionsParameters` objects. See [extended auhorizations](https://stripe.com/docs/terminal/features/extended-authorizations#authorization-validity) and [incremental authorizations](https://stripe.com/docs/terminal/features/incremental-authorizations)
- Beta: USB connectivity is now available via `Terminal.connectUsbReader` for M2 reader. Note that this API isn't
  finalized and may be changed. As a result, it requires use of a new `@OptIn` annotation; `@UsbConnectivity`.

## 2.7.1 - 2022-03-21

- Fix: Resolved an issue causing the SDK to become unresponsive during payment collection.

## 2.7.0 - 2022-02-28

- New: `CollectConfiguration` object to provide an option to skip tipping when
  calling `Terminal.collectPaymentMethod`. It requires the use of a new `@OptIn`
  annotation; `@OnReaderTips`. See
  [Collect on-reader tips](https://stripe.com/docs/terminal/features/collecting-tips/on-reader)
  for details. Note that on-reader tips is in beta.
- New: Added `onBatteryLevelUpdate` callback in `ReaderListener` both for Bluetooth and USB readers when connected.
  It reports battery info for every 10 minutes. See [issue 199](https://github.com/stripe/stripe-terminal-android/issues/199)
- New: The Example apps can now connect to internet readers. See [issue 174](https://github.com/stripe/stripe-terminal-android/issues/174) for details.
- Fix: Removed Android 12 Bluetooth permissions from the Android manifest. This
  fixes a Bluetooth-related permissions exception that was happening on Android
  12 devices when the application did not explicitly request the permisions.
- Fix: `ReaderListener.onReportLowBatteryWarning` can now be invoked during connect. See [issue 175](https://github.com/stripe/stripe-terminal-android/issues/175) for details.
- Beta: USB connectivity is now available via `Terminal.connectUsbReader` for Chipper and WP3 readers. Note that this API isn't
  finalized and may be changed. As a result, it requires use of a new `@OptIn` annotation;
  `@UsbConnectivity`.

## 2.6.0 - 2022-01-24

- Fix: Resolved an issue where connecting to a WP3 immediately after an M2 can cause update failures.
- Fix: Stripe M2 Bluetooth pairing dialog is no longer displayed twice.
- Fix: Resolved NullPointerException thrown when `BluetoothDevice.name` isn't available during discovery. See [issue 196](https://github.com/stripe/stripe-terminal-android/issues/196) for details.
- Fix: Resolved an issue with Gson not being included as an explicit dependency. See [issue 188](https://github.com/stripe/stripe-terminal-android/issues/188) for details.
- Fix: Updated R8 keep rules to resolve an issue where Stripe M2 readers would fail to connect with minification enabled.
- Fix: Added Bluetooth scan rate limiting to avoid Android SDK silently failing when the scan rate limit is exceeded.
- Update: Added Bluetooth scan error handling and retries when scans fail due to `ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES` and `ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY`.
- Fix: Resolved an issue where errors thrown by the BBPOS SDK during Bluetooth reader updates can cause a deadlock and break future discovery attempts.

## 2.5.2 - 2021-11-22

- Fix: Calling `discoverReaders` with `DiscoveryMethod.BLUETOOTH_SCAN` returns local Bluetooth readers regardless of whether or not the SDK has internet connectivity.
- Fix: Manual transaction cancellation during online processing now works as expected. See [issue 181](https://github.com/stripe/stripe-terminal-android/issues/181) for details.
- Fix: Pre-dipping immediately after connecting to a Bluetooth reader now works as expected. See [issue 182](https://github.com/stripe/stripe-terminal-android/issues/182) for details.

## 2.5.1 - 2021-11-16

- Fix: Pre-dipping following a cancelled transaction now works as expected. See [issue 179](https://github.com/stripe/stripe-terminal-android/issues/179) for details.

## 2.5.0 - 2021-11-15

- Fix: Pre-dipping now works as expected with Chippers. See [issue 173](https://github.com/stripe/stripe-terminal-android/issues/173) for details.
- Fix: Failure to issue a card-present refund will now invoke error callbacks properly.

## 2.4.1 - 2021-10-25

- Fix: Removed Android 12 Bluetooth permissions. See [issue 171](https://github.com/stripe/stripe-terminal-android/issues/171) for details.

## 2.4.0 - 2021-10-21

- New: Strong Customer Authentication (SCA) support was added for internet readers.
- Update: EMV online processing timeout increased from 15s to 30s. Note that this timeout isn't
  used by Chipper devices.
- Remove: Remove Machine Driven Registration. It's been moved to the DeviceManagementSDK.
- Fix: Amex cards no longer decline when used via Apple Pay. See [issue 166](https://github.com/stripe/stripe-terminal-android/issues/166) for details.

## 2.3.1 - 2021-09-22

- Fix: Resolved an issue with class loading in the SDK

## 2.3.0 - 2021-09-21

**Note**: This release has an issue with loading internal classes and should not be used. Update to 2.3.1 instead.

- Fix: Resolved issue that led to some optional reader updates being incorrectly marked as required.

- Fix: Removed `ClassNotFoundException: com.stripe.cots.CotsAdapterProvider` stacktrace on SDK
  initialization. See [issue 155](https://github.com/stripe/stripe-terminal-android/issues/155) for details.

- New: Use `retrieveSetupIntent` to get any SetupIntents that were created outside of your app.

- Update: fields in the `Reader` class have been added/removed. All newly added fields correspond to the same values
  returned by the [Stripe API](https://stripe.com/docs/api/terminal/readers/object)
    - Added `id`, `networkStatus`, `label`, `baseUrl`, `ipAddress`, `livemode`
    - Removed `ipReader`, `cotsDescriptor`

## 2.2.0 - 2021-08-23

- Fix: Resolved intermittent unexpected token invalidation errors when using simlated
  readers, and when a connected reader is left idle for a long period of time prior to completing a transaction.

- Fix: Duplicate class conflict with Firebase and other libraries that use `protobuf-javalite` resolved.
  See [issue 135](https://github.com/stripe/stripe-terminal-android/issues/135) for details.

- New: [Setup Future Usage](https://stripe.com/docs/api/payment_intents/create#create_payment_intent-setup_future_usage)
  field added to `PaymentIntentParameters`.

- Fix: When using a simulated BBPOS reader, starting, cancelling, and restarting an update now works as expected.

- Update: Location updates are now only requested when the application consuming the SDK is foregrounded.

- Fix: throw `TerminalException` if `processPayment` gets a success HTTP request (code=200) but with
  `lastPaymentError` being non-null, since it's also a form of decline.

- Update: we will use more precise and consistent `TerminalErrorCode` and error messages for failures of all confirmation operations,
  including processRefund, processPayment, confirmSetupIntent. Specifically,
  For `TerminalErrorCode`:
    - Use `UNSUPPORTED_SDK` if stripe server declined with `terminal_unsupported_sdk_version`.
    - Use `UNSUPPORTED_READER_VERSION` if stripe server declined with `terminal_unsupported_reader_version`.
    - Use `API_SESSION_EXPIRED` if stripe server declined with `api_key_expired`.
    - Use new enum code `STRIPE_API_CONNECTION_ERROR` for general internet connection failures or timeouts.
    - Use `DECLINED_BY_STRIPE_API` for other failures with decode-able responses.
      Comparing to in the past we:
    - Used `STRIPE_API_ERROR` for internet connection failures and all failures with decode-able responses for processRefund.
    - Used `DECLINED_BY_STRIPE_API` for internet connection failures and all failures with decode-able responses for processPayment.
    - Used `DECLINED_BY_STRIPE_API` for internet connection failures and all failures with decode-able responses for confirmSetupIntent.
      What's unchanged:
    - Use `STRIPE_API_RESPONSE_DECODING_ERROR` if SDK fails to decode response from Stripe service.
      For error messages:
    - Use "Could not connect  to Stripe. Please retry." for general connection errors, timeouts.
    - Use the message sent from server.
      Comparing to in the past we:
    - Always used "Stripe API error".

- Update: When discovering readers in our handoff integration mode, `discoverReaders` will only return a
  reader if it has been registered. If the current reader has not been registered, `discoverReaders`
  will return an empty list of readers.

- Fix: Each of the Terminal SDK AARs now contain keep rules. This allows you to enable Proguard/R8
  minification without configuring your own keep rules. See [issue 142](https://github.com/stripe/stripe-terminal-android/issues/142) for details.

- New: Added `generateRegistrationCode` for generating a smart reader registration code without human
  intervention. Note that this functionality is only available when the SDK is running directly on a
  smart reader device.

- New: Support displaying transaction information on-screen for internet readers using
  `Terminal.setReaderDisplay` and `Terminal.clearReaderDisplay`.

- New: `Cart` and `CartLineItem` classes have been added to hold transaction
  information used for setting the reader display

## 2.1.0 - 2021-08-02

- New: Added `STRIPE_M2` to `DeviceType`. This reader is in beta testing and
  not yet generally available.

## 2.0.0 - 2021-06-23

### Summary

The Stripe Terminal Android SDK has been updated to support new readers, global
payment processing, and enhanced connectivity and update behavior. To support
this new functionality, we’ve made a number of changes, some of which are
backwards-incompatible with the current Stripe Terminal Mobile SDKs.

If you’re upgrading from a 1.x release, here’s what you need to know:

- Connecting to a reader has changed.
- We now support using
  [locations](https://stripe.com/docs/api/terminal/locations) with Bluetooth
  readers like the Chipper 2X or WisePad 3.
- Use of locations is now required.
- In some cases, Stripe might need to push an update to readers for security or
  compliance reasons; your integration must now support a new optional /
  required update flow.

For upgrading an integration from an older release, see our [SDK migration
guide](https://stripe.com/docs/terminal/sdk-migration-guide)

### Connecting to a reader

`connectReader` has been split into two separate methods: `connectBluetoothReader` and `connectInternetReader`.

#### `connectBluetoothReader`

connectBluetoothReader should be used to connect to any Bluetooth reader like the BBPOS WisePad 3 and BBPOS Chipper 2X BT.

- `connectBluetoothReader` requires providing a new `BluetoothReaderListener`
  which is used to report all reader events.
- The `onReportReaderEvent` and `onReportLowBatteryWarning` methods have been
  moved from the `TerminalListener` to the `BluetoothReaderListener`.
- `ReaderDisplayListener` has been removed and the `onRequestReaderInput` and
  `onRequestReaderDisplayMessage` methods have been moved to the
  `BluetoothReaderListener`.
- `ReaderSoftwareUpdateListener` has been removed, and the
  `BluetoothReaderListener` is used for communicating to your app about updates
  for the reader.

See [Updating reader software: Required
updates](https://stripe.com/docs/terminal/readers/bbpos-chipper2xbt#required-updates)
for more details.

#### `connectInternetReader`

`connectInternetReader` should be used to connect to Internet connected
countertop readers like the Verifone P400 or the BBPOS WisePOS E. There are no
changes required between `connectReader` and `connectInternetReader` for your
countertop reader integration.

## Bluetooth reader updates

The `checkForUpdate` method has been removed. The Stripe Terminal SDK now
checks for required and optional updates during reader connection.

Required update installation is reported to the new `BluetoothReaderListener`
with the `onStartInstallingUpdate` method. Progress will be reported to that
same delegate with `onReportReaderSoftwareUpdateProgress`. When the
installation of the required update finishes the delegate will receive the
`onFinishInstallingUpdate`.

For more information about implementing reader updates, see [Updating reader
software](https://stripe.com/docs/terminal/readers/bbpos-chipper2xbt#updating-reader-software)
and [simulated reader
updates](https://site-admin.stripe.com/docs/terminal/testing#simulated-reader-updates).


## Bluetooth reader locations

Like Internet readers, Bluetooth readers must now be registered to
[Locations](https://stripe.com/docs/api/terminal/locations). Registering your
Bluetooth readers to a location ensures that the readers install the proper
regional configurations and are properly grouped on your account.

To register the reader to a location, create and use a
`BluetoothConnectionConfiguration` object with the locationId set accordingly,
and pass that object in when calling `Terminal.connectBluetoothReader`.

When discovering readers that have already been registered to a location, those
reader objects will have a valid `locationId` property during discovery. If it
makes sense for your application, you can pass that locationId from the
discovered `Reader` object into the `BluetoothConnectionConfiguration`
initializer to keep that reader registered to the same location. This is the
pattern we recommend when discovering and connecting to simulated Bluetooth
readers, which are now automatically registered to a mock location.

When connecting to a reader, you may want to display a list of Locations in
your app. To enable this, the SDK provides the `Terminal.listLocations` method
that takes the same parameters as the [List all
Locations](https://stripe.com/docs/api/terminal/locations/list) API endpoint.
You may want to adjust your connection flow to allow users to pick a location
before they select a reader, should they want to switch that reader's location
manually.

## Detailed changes

- `installUpdate` has been replaced by a no-argument `installAvailableUpdate`
  method, which installs the update stored on the connected reader's
  `availableUpdate`.
- Incremental updates are now treated the same as required updates, resulting in the following
  changes:
    - Removed `UPDATING` from `ConnectionStatus`. All updates are now exclusively communicated via the
      `BluetoothReaderListener`. Required updates that are installed while connecting to a reader will
      be performed while the `ConnectionStatus` is `CONNECTING`.
    - All required reader software updates now report
      `BluetoothReaderListener.onStartInstallingUpdate` and the `Cancelable` provided is now
      nullable. When an incremental-only change is announced, the `Cancelable` will be null since
      these incremental changes are required and cannot be canceled.
    - `ReaderSoftwareUpdate` now has a `components` property that can be used to determine the changes
      that will be applied with this update. `version` can still be used to identify the specific
      firmware, config, and keys that will be installed with the update.
    - On `SimulateReaderUpdate`, replaced `REQUIRED_INCREMENTAL` `REQUIRED_FULL` with `REQUIRED` since
      these update types now behave the same.
- We have changed our packaging for models and callables:
    - Models have been moved from `com.stripe.stripeterminal.model` to
      `com.stripe.stripeterminal.external.models`.
    - Callables have been moved from `com.stripe.stripeterminal.callable` to
      `com.stripe.stripeterminal.external.callable`.
- Replaced `TerminalLifecycleObserver` with `TerminalApplicationDelegate`. Users now must invoke
  `TerminalApplicationDelegate.onCreate` and `TerminalApplicationDelegate.onTrimMemory` from their
  applications.
- Fixed `BluetoothReaderListener.onReportReaderEvent` to properly report card insertion & removal
  events.
- Updated amount types from `Int` to `Long` for the following fields:
    - `PaymentIntent.amount`, `PaymentIntent.amountCapturable`, `PaymentIntent.amountReceived`, &
      `PaymentIntent.applicationFeeAmount`
    - `PaymentIntentParameters.amount` & `PaymentIntentParameters.applicationFeeAmount`
    - `Charge.amount`, `Charge.amountRefunded`, & `Charge.applicationFeeAmount`
    - `RefundParameters.amount`
    - `Refund.amount`
    - `SingleUseMandate.amount`
- Renamed error code `PAYMENT_DECLINED_BY_STRIPE_API` to `DECLINED_BY_STRIPE_API`
- Renamed error code `PAYMENT_DECLINED_BY_READER` to `DECLINED_BY_READER`
- When a reader unexpectedly disconnects during an operation, we swapped the callback order for
  `TerminalListener.onUnexpectedReaderDisconnect` and `Callback.onFailure`. Now,
  `Callback.onFailure` will be called first and `TerminalListener.onUnexpectedReaderDisconnect` will
  be called second. This fixes a bug where we incorrectly failed any new operation started within
  the context of a `TerminalListener.onUnexpectedReaderDisconnect` callback.

## New features

- Invite only: Added support for creating SetupIntents with the WisePad 3.
- Added support for Interac transactions and refunds
- Added support for setting a `SimulatedCard` on the `SimulatorConfiguration`
  object, allowing simulation of different card brands and decline types
- Added support for the P400 in the U.S.

## 1.0.22 - 2021-06-15
- Bump vendor SDK

## 1.0.18 - 2021-03-23
- Fix [issue 128](https://github.com/stripe/stripe-terminal-android/issues/128) by upgrading and
  unshading OkHTTP to version 4.7.2 in the SDK.

## 1.0.17 - 2020-05-29
- Fix [issue 109](https://github.com/stripe/stripe-terminal-android/issues/109)

## 1.0.15 - 2020-04-06
- Fix [issue 99](https://github.com/stripe/stripe-terminal-android/issues/99)
- Fix [issue 103](https://github.com/stripe/stripe-terminal-android/issues/103)
- Fix EventFragment in Java example app showing empty screen
- Fix UpdateReaderFragment in Java example app showing empty screen

## 1.0.14 - 2020-03-27
- Fix [bug](https://github.com/stripe/stripe-terminal-android/issues/73) where Android Studio cannot find `stripeterminal` classes

## 1.0.13 - 2020-03-23
- Remove extraneous imports in our sample Java app
- Remove unnecessary tests in both our example apps

## 1.0.12 - 2020-03-16
- No changes relative to 1.0.11

## 1.0.11 - 2020-03-16
- Change .aar name, but no external user changes since 1.0.9

## 1.0.10 - 2020-03-16
- No change relative to 1.0.9

## 1.0.9 - 2020-03-16
- Fixes a bug when launching the example app with location turned off would crash on start
- Updated gradle dependencies for various libraries
- Introduce `cardholder_name` as part of `CardPresentDetails`

## 1.0.8 - 2020-01-31
- Fixes Retrofit absolute path [bug](https://github.com/stripe/stripe-terminal-android/issues/82)

## 1.0.7 - 2020-01-30
- No changes relative to 1.0.2; we've migrated our deploy process internally, and this was a no-op deploy to test the new publishing mechanism

## 1.0.6 - 2020-01-30
- No changes relative to 1.0.2; we've migrated our deploy process internally, and this was a no-op deploy to test the new publishing mechanism

## 1.0.2 - 2020-01-21
- Update gradle dependencies to work on latest gradle plugin
- Example apps (databinding specifically) now compile on latest gradle plugin
- Minor polish for multi-activity apps' lifecycle automatic cleanup

## 1.0.1 - 2019-11-12
- Fixed race condition in multi-activity apps that could lead to DB exception

## 1.0.0 - 2019-10-22
- Fixed timeout functionality in `discoverReaders`

## 1.0.0-rc2 - 2019-10-03
**Made SDK compatible with React Native**
The Stripe Terminal Android SDK uses OkHttp 4.x, while React Native uses 3.x, which was causing a
collision. This release hides our use of OkHttp, so React Native should no longer cause problems.

## 1.0.0-rc1 - 2019-09-09
**Switched to BLE for reader discovery**
This should speed up discovery times slightly, and will prevent some edge cases where the user has
connected to the reader via their device's settings menu, since BLE devices aren't able to connect
from that menu.

**Made the SDK lifecycle aware**
We've made the SDK lifecycle aware so that we can optimize resource use according to what stage of
life the app is in. This change will also give us more debugging info for any future issues.
NOTE: With this change, you'll need to register our lifecycle observer in a few of your
`Application` subclass' methods. See [the installation guide](https://github.com/stripe/stripe-terminal-android#installation)
for more information.

**Added a static `isInitialized` method to `Terminal`**
This can be used to check if there's an existing Terminal object before calling `getInstance`.

**Other changes**
- Made all user-facing objects Parcelable
- Reorganized SDK into packages, so you might need to update your imports
