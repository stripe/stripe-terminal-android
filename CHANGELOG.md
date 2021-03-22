# CHANGELOG

## 1.0.18 - 2021-03-23
- Fix [issue 128](https://github.com/stripe/stripe-terminal-android/issues/128) by upgrading and unshading OkHTTP to version 4.7.2 in the SDK.

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
