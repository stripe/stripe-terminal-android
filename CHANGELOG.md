# CHANGELOG

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
  [locations](https://stripe.com/docs/api/terminal/locations) with bluetooth
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
