# 1.0.2

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:1.0.2"
```

## Other changes
- Update gradle dependencies to work on latest gradle plugin
- Example apps (databinding specifically) now compile on latest gradle plugin
- Minor polish for multi-activity apps' lifecycle automatic cleanup

# 1.0.1

## Other changes
- Fixed race condition in multi-activity apps that could lead to DB exception

# 1.0.0

## Other changes
- Fixed timeout functionality in `discoverReaders`

# 1.0.0-rc2

## Made SDK compatible with React Native
The Stripe Terminal Android SDK uses OkHttp 4.x, while React Native uses 3.x, which was causing a
collision. This release hides our use of OkHttp, so React Native should no longer cause problems.

# 1.0.0-rc1

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
