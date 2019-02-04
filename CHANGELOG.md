0.4

If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:0.4"
```

## Update to minSdkVersion

Due to requests from a few of our Alpha users, we've decided to support API versions down to 21, instead of the 24 we had previously. We hope that gives you the flexibility to use the Terminal SDK on more devices.

## Singleton initializer

`Terminal` is now a singleton, so you will need to update your integration.

Before:
```
// Create your listener object. Override any methods that you want to be notified of.
TerminalListener listener = new TerminalListener() {};

// Create your token provider.
MyTokenProvider tokenProvider = new MyTokenProvider();

// Create an instance of TerminalConfiguration
TerminalConfiguration config = new TerminalConfiguration(LogLevel.VERBOSE);

// Initialize your Terminal instance
Terminal terminal = Terminal.initTerminal(getActivity(), config, tokenProvider, listener);
```

After:
```
// Create your listener object. Override any methods that you want to be notified of.
TerminalListener listener = new TerminalListener() {};

// Create your token provider.
MyTokenProvider tokenProvider = new MyTokenProvider();

// Initialize your Terminal instance
Terminal.initTerminal(getActivity(), LogLevel.VERBOSE, tokenProvider, listener);

// Retrieve the Terminal instance any time you need it
Terminal.getInstance();
```

If you were relying on destroying and recreating `Terminal` instances to clear state (for example, to switch between different Stripe accounts in your app), you should instead use the `clearCachedCredentials` method.

As noted in the code example above, `TerminalConfiguration` has also been removed, and the `logLevel` parameter that was previously passed into the `TerminalConfiguration` constructor is now passed into `initTerminal` directly.



