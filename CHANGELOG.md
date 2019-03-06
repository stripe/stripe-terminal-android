0.5
  
If you're using Gradle, update your build file to:

```
implementation "com.stripe:stripeterminal:0.5"
```

## Update to Example App

The example app has been rewritten in Kotlin to be simpler and more feature-rich than the existing app.

## Added Simulator

A `simulated` flag has been added to the `DiscoveryConfiguration` constructor. When set, this will allow you to discover a simulated version of the device you specify. 

Once you connect to this device, it will handle all interactions with the reader for you, allowing you to quickly iterate on your integration. Here's an example showing how to use the simulator:

```
// Create a simulated configuration object
DiscoveryConfiguration config = new DiscoveryConfiguration(0, DeviceType.CHIPPER_2X, true);

// Create a ReaderDiscoveryListener
ReaderDiscoveryListener listener = new ReaderDiscoveryListener();

// Create a discovery Callback
Callback callback = new Callback();

// Request discovery of a simulated Chipper 2X reader
Terminal.getInstance().discoverReaders(config, listener, callback);
```
